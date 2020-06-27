package com.looseboxes.webform.services;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.Form;
import com.bc.webform.functions.FormInputContext;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.Wrapper;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.form.DependentsProvider;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.FormConfigBean;
import org.springframework.lang.Nullable;
import com.looseboxes.webform.entity.EntityConfigurer;
import com.looseboxes.webform.entity.EntityConfigurerService;
import com.looseboxes.webform.entity.EntityRepositoryProvider;

/**
 * @author hp
 */
@Service
public class FormService implements Wrapper<StoreDelegate, FormService>, FormFactory{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormService.class);

    public static final String FORM_ID_PREFIX = "form";
    
    private final ModelObjectService modelObjectService;
    private final EntityRepositoryProvider entityRepositoryFactory;
    private final TypeFromNameResolver typeFromNameResolver;
    private final FormAttributeService formAttributeService;
    private final FormFactory formFactory;
    private final FormInputContext<Object, Field, Object> formInputContext;
    private final EntityConfigurerService modelObjectConfigurerService;
    private final DependentsProvider dependentsProvider;

    @Autowired
    public FormService(
            ModelObjectService modelObjectService,
            EntityRepositoryProvider entityRepositoryFactory, 
            TypeFromNameResolver typeFromNameResolver,
            FormAttributeService formAttributeService,
            FormFactory formFactory,
            FormInputContext<Object, Field, Object> formInputContext,
            EntityConfigurerService modelObjectConfigurerService,
            DependentsProvider dependentsProvider) {
        this.modelObjectService = Objects.requireNonNull(modelObjectService);
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
        this.formAttributeService = Objects.requireNonNull(formAttributeService);
        this.formFactory = Objects.requireNonNull(formFactory);
        this.formInputContext = Objects.requireNonNull(formInputContext);
        this.modelObjectConfigurerService = Objects.requireNonNull(modelObjectConfigurerService);
        this.dependentsProvider = Objects.requireNonNull(dependentsProvider);
    }

    @Override
    public FormService wrap(StoreDelegate delegate) {
        return new FormService(
                this.modelObjectService,
                this.entityRepositoryFactory,
                this.typeFromNameResolver,
                this.formAttributeService.wrap(delegate),
                this.formFactory,
                this.formInputContext,
                this.modelObjectConfigurerService,
                this.dependentsProvider
        );
    }

    @Override
    public StoreDelegate unwrap() {
        return this.formAttributeService.unwrap();
    }
    
    public void checkAll(FormConfig formConfig){

        Objects.requireNonNull(formConfig.getCrudAction());
        Objects.requireNonNull(formConfig.getFormid());
        final String modelname = Objects.requireNonNull(formConfig.getModelname());
        final Object modelobject = Objects.requireNonNull(formConfig.getModelobject());
        
        final String foundname = this.typeFromNameResolver.getName(modelobject.getClass());
        
        if( ! modelname.equalsIgnoreCase(foundname)) {
            
            LOG.warn("Expected name: {}, found name: {} from type: {}", 
                    modelname, foundname, modelobject.getClass());
        
            throw Errors.unexpectedModelName(modelname, foundname);
        }
    }

    public FormConfigBean onShowform(FormConfigBean formConfig) {
        
        final Object modelobject;
        
        // Form id is often passed to a form at the first stage.
        // This happens when the form is being returned to after some tangential
        // action, usually the tangential action is carried out via another form
        //
        final boolean newForm = formConfig.getFormid() == null;
        if(newForm) {
            
            modelobject = this.configureModelObject(
                    modelObjectService.getModel(formConfig));
            
            formConfig.setFormid(this.generateFormId());
            
        }else{
            modelobject = null;
        }
        
        return this.update(formConfig, ! newForm, modelobject);
    }
    
    /**
     * Apply custom configurations to the modelobject.
     * @param modelobject 
     */
    public Object configureModelObject(Object modelobject) {
        
        Objects.requireNonNull(modelobject);

        // Custom configuration for the newly created model object
        //
        final EntityConfigurer configurer = 
                modelObjectConfigurerService.getConfigurer(
                        modelobject.getClass()).orElse(null);
        if(configurer != null) {
            return configurer.configure(modelobject);
        }
        
        return modelobject;
    }
    
    public FormConfigBean onValidateForm(FormConfigBean formConfig, Object modelobject) {
        
        return this.update(formConfig, true, modelobject);
    }
    
    public FormConfigBean onSubmitForm(FormConfigBean formConfig) {
        
        return this.update(formConfig, true, null);
    }
    
    private FormConfigBean update(
            FormConfigBean formConfig,
            boolean existingForm, 
            @Nullable Object modelobject) {
        
        final CRUDAction action = formConfig.getCrudAction();
        final String formid = formConfig.getFormid();
        final String modelname = formConfig.getModelname();
        final String modelid = formConfig.getModelid();
        final String parentFormId = formConfig.getParentFormid();
        
        if(CRUDAction.create != action && modelid == null) {
            throw new AttributeNotFoundException(modelname, Params.MODELID);
        }
        
        FormConfig existingFormConfig = ! existingForm ?
                this.formAttributeService.getSessionAttribute(formid, null) :
                this.formAttributeService.getSessionAttributeOrException(formid);
        
        LOG.debug("Existing params: {}\nexisting form: {}", existingFormConfig,
                (existingFormConfig==null?null:existingFormConfig.getForm()));

        if(existingForm && existingFormConfig == null) {
            throw new InvalidRouteException();
        }

        final Form parentForm = parentFormId == null ? null : 
                this.formAttributeService.getFormOrException(parentFormId);
        
        if (modelobject == null) {
            modelobject = existingFormConfig.getModelobject();
        }

        final Form form = this.newForm(parentForm, formid, modelname, modelobject);

        if(existingFormConfig == null) {
            
            return formConfig.writableCopy().form(form);

        }else{
        
            this.validate(existingFormConfig, formConfig);
            
            return existingFormConfig.writableCopy().form(form);
        }
    }   
    
    public void validate(FormConfig existing, FormConfig fromHttpRequest) {
        this.validate(Params.ACTION, existing.getCrudAction(), fromHttpRequest.getCrudAction());
        this.validate(Params.FORMID, existing.getFormid(), fromHttpRequest.getFormid());
        this.validate(Params.MODELNAME, existing.getModelname(), fromHttpRequest.getModelname());
        this.validate(Params.MODELID, existing.getModelid(), fromHttpRequest.getModelid());
        // This is changes with each request
//        this.validate(existing.getModelobject(), fromHttpRequest.getModelobject());
        this.validate(Params.MODELFIELDS, existing.getModelfields(), fromHttpRequest.getModelfields());
        this.validate(Params.PARENT_FORMID, existing.getParentFormid(), fromHttpRequest.getParentFormid());
        this.validate(Params.TARGET_ON_COMPLETION, existing.getTargetOnCompletion(), fromHttpRequest.getTargetOnCompletion());
    }
    
    public void validate(String name, Object expected, Object found) {
        if( ! Objects.equals(expected, found)) {
            throw this.invalidRouteException(name, expected, found);
        }
    }
    
    private InvalidRouteException invalidRouteException(
            String name, Object expected, Object found) {
        throw new InvalidRouteException(
                "For: " + name + "\nExpected: " + expected + "\n   Found: " + found);
    }
    
    /**
     * Form ids need to be unique within a session.
     * @return 
     */
    public String generateFormId() {
        return FORM_ID_PREFIX + Long.toHexString(System.currentTimeMillis());
    }

    @Override
    public Form newForm(Form parentForm, String id, String name){
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        return formFactory.newForm(parentForm, id, name);
    }

    @Override
    public Form newForm(Form parentForm, String id, String name, Object object) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(object);
        return formFactory.newForm(parentForm, id, name, object);
    }
    
    /**
     * Mirror for {@link #getFormAttributeService()}
     * @return The {@link com.looseboxes.webform.services.FormAttributeService}
     * @see #getFormAttributeService() 
     */
    public FormAttributeService attributeService() {
        return formAttributeService;
    }

    /**
     * @return The {@link com.looseboxes.webform.services.FormAttributeService}
     */
    public FormAttributeService getFormAttributeService() {
        return formAttributeService;
    }
}
/**
 * 
    public void validateType(String name, Object lhs, Object rhs) {
        if(lhs == null && rhs != null || lhs != null && rhs == null) {
            throw this.invalidRouteException(name, lhs, rhs);
        }else if(lhs != null && rhs != null) {
            this.validate(name, lhs.getClass(), rhs.getClass());
        }
    }
 * 
 */