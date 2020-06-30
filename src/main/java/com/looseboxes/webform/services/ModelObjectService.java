package com.looseboxes.webform.services;

import com.bc.webform.Form;
import com.bc.webform.FormBuilder;
import com.bc.webform.FormMember;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.web.FormConfigBean;
import org.springframework.lang.Nullable;
import com.looseboxes.webform.entity.EntityConfigurerService;
import com.looseboxes.webform.form.UpdateParentFormWithNewlyCreatedModel;
import com.looseboxes.webform.web.FormRequest;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hp
 */
@Service
public class ModelObjectService{
    
    private final Logger log = LoggerFactory.getLogger(ModelObjectService.class);

    public static final String FORM_ID_PREFIX = "form";
    
    @Autowired private ModelObjectProvider modelObjectProvider;
    @Autowired private FormBuilder<Object, Field, Object> formBuilder;
    @Autowired private EntityConfigurerService modelObjectConfigurerService;
    @Autowired private UpdateParentFormWithNewlyCreatedModel parentFormUpdater;

    public <T> FormRequest<T> onBeginForm(FormRequest<T> formRequest) {
        
        final T modelobject;
        
        final FormConfigBean formConfig = formRequest.getFormConfig();
        
        // Form id is often passed to a form at the first stage.
        // This happens when the form is being returned to after some tangential
        // action, usually the tangential action is carried out via another form
        //
        final boolean newForm = formConfig.getFormid() == null;
        if(newForm) {
            
            modelobject = this.configureModelObject(
                    (T)this.modelObjectProvider.getModel(formConfig), formRequest);
            
            formConfig.setFormid(this.generateFormId());
            
        }else{
            modelobject = null;
        }
        
        return this.update(formRequest, ! newForm, modelobject);
    }
    
    /**
     * Apply custom configurations to the modelobject.
     * @param modelobject 
     * @param formRequest
     * @return  
     */
    public <T> T configureModelObject(T modelobject, FormRequest<T> formRequest) {
        
        final Class<T> type = (Class<T>)modelobject.getClass();

        // Custom configuration for the newly created model object
        //
        return modelObjectConfigurerService.getConfigurer(type)
                        .map((configurer) -> configurer.configure(modelobject, formRequest))
                        .orElse(modelobject);
    }
    
    public <T> FormRequest<T> onValidateForm(FormRequest<T> formRequest, T modelobject) {
        
        return this.update(formRequest, true, modelobject);
    }
    
    public <T> FormRequest<T> onSubmitForm(FormRequest<T> formRequest) {
        
        return this.update(formRequest, true, null);
    }
    
    private <T> FormRequest<T> update(
            FormRequest<T> formRequest, boolean existingForm, @Nullable T modelobject) {
        
        FormConfigBean formConfig = formRequest.getFormConfig();
        
        final CRUDAction action = formConfig.getCrudAction();
        final String formid = formConfig.getFormid();
        final String modelname = formConfig.getModelname();
        final String modelid = formConfig.getModelid();
        final String parentFormId = formConfig.getParentFormid();
        
        if(CRUDAction.create != action && modelid == null) {
            throw new AttributeNotFoundException(modelname, Params.MODELID);
        }
        
        final FormAttributeService formAttributeService = formRequest.getAttributeService();
        
        FormConfigBean existingFormConfig = ! existingForm ?
                formAttributeService.getSessionAttribute(formid, null) :
                formAttributeService.getSessionAttributeOrException(formid);
        
        log.debug("Existing params: {}\nexisting form: {}", existingFormConfig,
                (existingFormConfig==null?null:existingFormConfig.getForm()));

        if(existingForm && existingFormConfig == null) {
            throw new InvalidRouteException();
        }

        final Form parentForm = parentFormId == null ? null : 
                formAttributeService.getFormOrException(parentFormId);
        
        if (modelobject == null) {
            modelobject = (T)existingFormConfig.getModelobject();
        }

        final Form form = this.newForm(parentForm, formid, modelname, modelobject);

        if(existingFormConfig == null) {
            
            formRequest.setFormConfig(formConfig.writableCopy().form(form));

        }else{
        
            existingFormConfig.validate(formConfig);
            
            formRequest.setFormConfig(existingFormConfig.writableCopy().form(form));
        }
        
        return formRequest;
    }   
    
    public boolean updateParentForm(FormRequest formRequest) {
        return this.parentFormUpdater.updateParent(formRequest);
    }
    
    /**
     * Form ids need to be unique within a session.
     * @return 
     */
    public String generateFormId() {
        return FORM_ID_PREFIX + Long.toHexString(System.currentTimeMillis());
    }

    private <T> Form<T> newForm(Form<T> parentForm, String id, String name, T domainObject) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(domainObject);
        final Form form = this.formBuilder
                .applyDefaults(name)
                .id(id)
                .parent(parentForm)
                .dataSource(domainObject)
                .build();

        this.logFormFields(form);

        return (Form<T>)form;
    }
    
    private void logFormFields(Form form) {
        if(log.isDebugEnabled()) {
            final Function<FormMember, String> mapper = (ff) -> {
                final Object value = ff.getValue();
                final Map choices = ff.getChoices();
                return ff.getName() + '=' + 
                        (choices==null||choices.isEmpty() ? value : 
                        (String.valueOf(value) + ", " + choices.size() + " choice(s)"));
            };
            log.debug("Form fields:{}", 
                    form.getMembers().stream()
                            .map(mapper)
                            .collect(Collectors.joining(", ")));
        }
    }
}
