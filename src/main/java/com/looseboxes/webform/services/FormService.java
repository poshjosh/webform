package com.looseboxes.webform.services;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.Form;
import com.bc.webform.FormMember;
import com.bc.webform.functions.FormInputContext;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.Wrapper;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.store.AttributeStore;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.exceptions.FormUpdateException;
import java.lang.reflect.Field;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.form.DependentsProvider;
import com.looseboxes.webform.form.DependentsUpdater;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.FormConfigDTO;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author hp
 */
@Service
public class FormService implements Wrapper<StoreDelegate, FormService>, FormFactory{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormService.class);

    public static final String FORM_ID_PREFIX = "form";
    
    private final ModelObjectService modelObjectService;
    private final EntityRepositoryFactory entityRepositoryFactory;
    private final TypeFromNameResolver typeFromNameResolver;
    private final AttributeService attributeService;
    private final FormFactory formFactory;
    private final FormInputContext<Object, Field, Object> formInputContext;
    private final DependentsProvider dependentsProvider;
    private final DependentsUpdater dependentsUpdater;

    @Autowired
    public FormService(
            ModelObjectService modelObjectService,
            EntityRepositoryFactory entityRepositoryFactory, 
            TypeFromNameResolver typeFromNameResolver,
            AttributeService attributeService,
            FormFactory formFactory,
            FormInputContext<Object, Field, Object> formInputContext,
            DependentsProvider dependentsProvider,
            DependentsUpdater dependentsUpdater) {
        this.modelObjectService = Objects.requireNonNull(modelObjectService);
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
        this.attributeService = Objects.requireNonNull(attributeService);
        this.formFactory = Objects.requireNonNull(formFactory);
        this.formInputContext = Objects.requireNonNull(formInputContext);
        this.dependentsProvider = Objects.requireNonNull(dependentsProvider);
        this.dependentsUpdater = Objects.requireNonNull(dependentsUpdater);
    }

    @Override
    public FormService wrap(StoreDelegate delegate) {
        return new FormService(
                this.modelObjectService,
                this.entityRepositoryFactory,
                this.typeFromNameResolver,
                this.attributeService.wrap(delegate),
                this.formFactory,
                this.formInputContext,
                this.dependentsProvider,
                this.dependentsUpdater
        );
    }

    @Override
    public StoreDelegate unwrap() {
        return this.attributeService.unwrap();
    }
    
    public void checkAll(FormConfig params){

        Objects.requireNonNull(params.getCrudAction());
        Objects.requireNonNull(params.getFormid());
        final String modelname = Objects.requireNonNull(params.getModelname());
        final Object modelobject = Objects.requireNonNull(params.getModelobject());
        
        final String foundname = this.typeFromNameResolver.getName(modelobject.getClass());
        
        if( ! modelname.equalsIgnoreCase(foundname)) {
            
            LOG.warn("Expected name: {}, found name: {} from type: {}", 
                    modelname, foundname, modelobject.getClass());
        
            throw Errors.unexpectedModelName(modelname, foundname);
        }
    }

    public FormConfig onUpdateDependentChoices(
            FormConfigDTO formConfig, Object modelobject, String propertyName, Locale locale) {
        
        Objects.requireNonNull(modelobject);
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(locale);

        formConfig.setModelobject(modelobject);
        
        formConfig = (FormConfigDTO)this.update(true, true, formConfig);
        
        Form form = Objects.requireNonNull(formConfig.getForm());
        
        final Map<Class, List> dependents = this.dependentsProvider
                .getDependents(modelobject, propertyName);
        
        for(Class memberType : dependents.keySet()) {
        
            final List dependentEntities = dependents.get(memberType);
            
            form = this.dependentsUpdater.update(form, memberType, dependentEntities, locale);
        }        
        
        formConfig.setForm(form);
        
        return formConfig;
    }
    
    public FormConfig onShowform(FormConfigDTO formConfigDTO) {
        
        formConfigDTO.setModelobject(modelObjectService.getModel(formConfigDTO));
        
        formConfigDTO.setFormid(this.generateFormId());
        
        return this.update(false, false, formConfigDTO);
    }

    public FormConfig onValidateForm(FormConfigDTO formConfigDTO, Object modelobject) {
        
        formConfigDTO.setModelobject(modelobject);

        return this.update(true, false, formConfigDTO);
    }
    
    public FormConfig onSubmitForm(FormConfigDTO formConfigDTO) {
        
        formConfigDTO.setModelobject(modelObjectService.getModel(formConfigDTO));
        
        return this.update(true, true, formConfigDTO);
    }
    
    public FormConfig update(
            boolean existingForm, 
            boolean useExistingModelObject,
            FormConfig formConfig) {
        
        final CRUDAction action = formConfig.getCrudAction();
        final String formid = formConfig.getFormid();
        final String modelname = formConfig.getModelname();
        final String modelid = formConfig.getModelid();
        Object modelobject = formConfig.getModelobject();
        final String parentFormId = formConfig.getParentFormid();
        
        if(CRUDAction.create != action && modelid == null) {
            throw new AttributeNotFoundException(modelname, Params.MODELID);
        }
        
        FormConfig existingFormConfig = ! existingForm ?
                this.getFormConfigAttribute(formid, modelname, null) :
                this.getFormConfigAttributeOrException(formid, modelname);
        
        LOG.debug("Existing params: {}\nexisting form: {}", existingFormConfig,
                (existingFormConfig==null?null:existingFormConfig.getForm()));

        if(existingForm && existingFormConfig == null) {
            throw new InvalidRouteException();
        }

        final Form parentForm = parentFormId == null ? null : 
                this.getFormOrException(parentFormId, modelname);
        
        if (useExistingModelObject) {
            modelobject = existingFormConfig.getModelobject();
        }

        final Form form = this.newForm(parentForm, formid, modelname, modelobject);

        if(existingFormConfig == null) {
            
            existingFormConfig = new FormConfig.Builder()
                    .with(formConfig)
                    .form(form)
                    .build();
        }else{
        
            this.validate(existingFormConfig, formConfig);
            
            existingFormConfig = this.update(existingFormConfig, form, modelobject);
        }
        
        return existingFormConfig;
    }   
    
    public void validate(FormConfig existing, FormConfig fromHttpRequest) {
    
        this.validate(Params.ACTION, existing.getCrudAction(), fromHttpRequest.getCrudAction());
        this.validate(Params.FORMID, existing.getFormid(), fromHttpRequest.getFormid());
        this.validate(Params.MODELNAME, existing.getModelname(), fromHttpRequest.getModelname());
        this.validate(Params.MODELID, existing.getModelid(), fromHttpRequest.getModelid());
        // This is changed with each stage of the form
//        this.validate(existing.getModelobject(), fromHttpRequest.getModelobject());
        //@TODO null and empty arrays/lists are equal
//        this.validate(Params.MODELFIELDS, existing.getModelfields(), fromHttpRequest.getModelfields());
        this.validate(Params.PARENT_FORMID, existing.getParentFormid(), fromHttpRequest.getParentFormid());
        this.validate(Params.TARGET_ON_COMPLETION, existing.getTargetOnCompletion(), fromHttpRequest.getTargetOnCompletion());
    }
    
    public void validate(String name, Object expected, Object found) {
        if( ! Objects.equals(expected, found)) {
            throw new InvalidRouteException(
                    "For: " + name + "\nExpected: " + 
                    expected + "\n   Found: " + found);
        }
    }
    
    public boolean updateParentWithNewlyCreated(FormConfig formConfig) 
            throws FormUpdateException{
        
        if(CRUDAction.create != formConfig.getCrudAction()) {
            throw new UnsupportedOperationException(
                    "Only 'create' supported but found: " + formConfig.getCrudAction());
        }

        final Form<Object> form = Objects.requireNonNull(formConfig.getForm());
        
        final Form<Object> parent = form.getParent();
        if(parent == null) {
            return false;
        }
        
        final String memberName = form.getName();
        final FormMember member = parent.getMemberOptional(memberName).orElse(null);
        if(member == null) {
            throw parentUpdateException(FormConfig.class, "name: "+memberName);
        }
        
        this.updateFormMember(form, memberName, formConfig.getModelobject());
        
        return true;
    }
    
    public void updateFormMember(Form form, String name, Object value) 
            throws FormUpdateException{
        
        final String formid = form.getId();
        
        final String modelname = form.getName();
        
        final FormConfig formReqParams = getFormConfigAttribute(
                formid, modelname, null);
        
        if(formReqParams == null) {
            throw parentUpdateException(FormConfig.class, 
                    String.format("formid: %1s, modelname: %2s", formid, modelname));
        }
        
        final Object modelobject = formReqParams.getModelobject();
        
        Objects.requireNonNull(modelobject);

        try{
            final Field field = modelobject.getClass().getField(name);
            Objects.requireNonNull(field);
            formInputContext.setValue(modelobject, field, value);
        }catch(NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
        
        final Form updateForm = newForm(form.getParent(), formid, modelname, modelobject);
        
        final FormConfig update = this.update(formReqParams, updateForm, modelobject);
        
        this.setSessionAttribute(update);
    }

    public FormConfig update(
            FormConfig formConfig, Form form, Object modelobject) {
        return new FormConfig.Builder()
                .with(formConfig)
                .form(form)
                .modelobject(modelobject)
                .build();
    }

    public FormUpdateException parentUpdateException(Class target, Object params) {
        return this.parentUpdateException(target.getSimpleName(), params);
    }
    
    public FormUpdateException parentUpdateException(String target, Object params) {
        final String msg = target + " not found for: " + params;
        return new FormUpdateException(msg);
    }

    /**
     * Form ids need to be unique within a session.
     * @return 
     */
    public String generateFormId() {
        return FORM_ID_PREFIX + Long.toHexString(System.currentTimeMillis());
    }

    public Form getFormOrException(String formid, String modelname) {
        
        Objects.requireNonNull(formid);
        Objects.requireNonNull(modelname);
        
        final FormConfig formConfig = 
                getFormConfigAttributeOrException(formid, modelname);
        
        final Form form = formConfig.getForm();
        
        if(form == null) {
            throw new InvalidRouteException();
        }
    
        LOG.trace("For id: {}, found form: {}", formid, formConfig);
        
        return form;
    }  
    
    public FormConfig getFormConfigAttributeOrException(
            String formid, String modelname) {
        
        Objects.requireNonNull(formid);
        Objects.requireNonNull(modelname);
        
        final FormConfig formReqParams = getSessionAttribute(formid, null);
        
        if(formReqParams == null) {
            throw new InvalidRouteException();
        }
        
        return formReqParams;
    }
    
    public FormConfig getFormConfigAttribute(
            String formid, String modelname, FormConfig resultIfNone) {
        Objects.requireNonNull(formid);
        Objects.requireNonNull(modelname);
        final FormConfig formConfig = formid == null ? 
                null : this.getSessionAttribute(formid, null);
        return formConfig == null ? resultIfNone : formConfig;
    }
    
    public FormConfig getSessionAttribute(
            String formid, FormConfig resultIfNone) {
        final String attributeName = this.getAttributeName(formid);
        Objects.requireNonNull(attributeName);
        final Object value = this.sessionAttributes()
                .getOrDefault(attributeName, null);
        LOG.trace("Get {} = {}", attributeName, value);
        return (FormConfig)value;
    }

    public void removeSessionAttribute(String formid) {
        Objects.requireNonNull(formid);
        final String name = getAttributeName(formid);
        final Object removed = sessionAttributes().remove(name);  
        LOG.trace("Removed {} = {}", name, removed);
    }
    
    public void setSessionAttribute(FormConfig formConfig){
        final String attributeName = this.getAttributeName(formConfig);
        Objects.requireNonNull(attributeName);
        this.sessionAttributes().put(attributeName, formConfig);
        LOG.trace("Set {} = {}", attributeName, formConfig);
    }

    public String getAttributeName(FormConfig formConfig) {
        return this.getAttributeName(formConfig.getFormid());
    }
    
    public String getAttributeName(String formid) {
        Objects.requireNonNull(formid);
        return HttpSessionAttributes.formReqParams(formid);
    }
    
    public AttributeStore<HttpSession> sessionAttributes() {
        return this.attributeService.sessionAttributes();
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
}
