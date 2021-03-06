package com.looseboxes.webform.services;

import com.bc.webform.form.Form;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.web.FormConfigDTO;
import org.springframework.lang.Nullable;
import com.looseboxes.webform.configurers.EntityConfigurerService;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.form.UpdateParentFormWithNewlyCreatedModel;
import com.looseboxes.webform.store.FormConfigStore;
import com.looseboxes.webform.web.FormRequest;
import java.util.Collections;
import javax.validation.ValidationException;

/**
 * @author hp
 */
@Service
public class ModelObjectService{
    
    private final Logger log = LoggerFactory.getLogger(ModelObjectService.class);

    public static final String FORM_ID_PREFIX = "form";
    
    @Autowired private ModelObjectProvider modelObjectProvider;
    @Autowired private FormFactory formFactory;
    @Autowired private EntityConfigurerService entityConfigurerService;
    @Autowired private UpdateParentFormWithNewlyCreatedModel parentFormUpdater;

    public <T> FormRequest<T> onBeginForm(FormConfigStore store, FormRequest<T> formRequest) {
        
        return this.onBeginForm(store, formRequest, this.getModelObject(formRequest));
    }
    
    private <T> T getModelObject(FormRequest<T> formRequest) {
        
        final T modelobject;
        
        final FormConfigDTO formConfig = formRequest.getFormConfig();
        
        // Form id is often passed to a form at the first stage.
        // This happens when the form is being returned to after some tangential
        // action, usually the tangential action is carried out via another form
        //
        final boolean newForm = formConfig.getFormid() == null;
        if(newForm) {
            modelobject = (T)this.modelObjectProvider.getModel(formConfig);
        }else{
            modelobject = null;
        }
        
        return modelobject;
    }
    
    public <T> FormRequest<T> onBeginForm(FormConfigStore store, FormRequest<T> formRequest, T modelobject) {
        
        final FormConfigDTO formConfig = formRequest.getFormConfig();

        final boolean newForm = formConfig.getFormid() == null;

        if(newForm) {
            formConfig.setFormid(this.generateFormId());
        }
        
        if(modelobject != null) {
            modelobject = this.configureModelObject(modelobject, formRequest);
        }
        
        return this.updateForm(store, formRequest, ! newForm, modelobject);
    }
    
    /**
     * Apply custom configurations to the model object.
     * @param modelobject 
     * @param formRequest
     * @return  
     */
    public <T> T configureModelObject(T modelobject, FormRequest<T> formRequest) {
        
        final Class<T> type = (Class<T>)modelobject.getClass();

        // Custom configuration for the newly created model object
        //
        return entityConfigurerService.getConfigurer(type)
                        .map((configurer) -> configurer.configure(modelobject, formRequest))
                        .orElse(modelobject);
    }
    
    public <T> FormRequest<T> onValidateForm(FormConfigStore store, FormRequest<T> formRequest, T modelobject) {
        
        return this.updateForm(store, formRequest, true, modelobject);
    }
    
    public <T> FormRequest<T> onSubmitForm(FormConfigStore store, FormRequest<T> formRequest) {
        
        return this.updateForm(store, formRequest, true, null);
    }
    
    public <S, T> FormRequest<T> updateRequest(
            FormRequest<S> formRequest, String modelname, String modelid) {
        
        FormConfigDTO formConfig = formRequest.getFormConfig();
        
        String parentFormId = formConfig.getFormid();
        
        FormConfigDTO formConfigUpdate = formConfig
                .fid(this.generateFormId())
                .form(null).id(modelid).modelfields(Collections.EMPTY_LIST)
                .modelname(modelname).parentfid(parentFormId)
                .targetOnCompletion(null);
        
        formRequest.setFormConfig(formConfigUpdate);
        
        return (FormRequest<T>)formRequest;
    }
    
    private <T> FormRequest<T> updateForm(
            FormConfigStore store, FormRequest<T> formRequest, boolean existingForm, @Nullable T modelobject) {
        
        FormConfigDTO formConfig = formRequest.getFormConfig();
        
        final CRUDAction action = formConfig.getCrudAction();
        final String formid = formConfig.getFormid();
        final String modelname = formConfig.getModelname();
        final String modelid = formConfig.getModelid();
        final String parentFormId = formConfig.getParentFormid();
        
        if(CRUDAction.create != action && modelid == null) {
            throw new AttributeNotFoundException(modelname, Params.MODELID);
        }
        
        FormConfigDTO existingFormConfig = ! existingForm ?
                store.getOrDefault(formid, null) :
                store.getOrException(formid);
        
        log.debug("Existing form config: {}\nexisting form: {}", existingFormConfig,
                (existingFormConfig==null?null:existingFormConfig.getForm()));

        if(existingForm && existingFormConfig == null) {
            throw new InvalidRouteException();
        }

        final Form parentForm = parentFormId == null ? null : 
                store.getFormOrException(parentFormId);
        
        if (modelobject == null) {
            modelobject = (T)existingFormConfig.getModelobject();
        }

        final Form form = formFactory.newForm(parentForm, formid, modelname, modelobject);

        if(existingFormConfig == null) {
            
            formRequest.setFormConfig(formConfig.form(form));

        }else{
  
            this.validate(existingFormConfig, formConfig);
            
            formConfig.merge(existingFormConfig);
            
            formRequest.setFormConfig(formConfig.form(form));
        }
        
        return formRequest;
    }   
    
    private void validate(FormConfigDTO existingFormConfig, FormConfigDTO receivedViaRequest) {
//        existingFormConfig.validate(receivedViaRequest);
        this.validateFormId(existingFormConfig, receivedViaRequest);
    }
    
    private void validateFormId(FormConfigDTO existingFormConfig, FormConfigDTO receivedViaRequest) {
//        existingFormConfig.validate(sentViaRequest);
        if( ! existingFormConfig.getFormid().equals(receivedViaRequest.getFormid())) {
            throw new ValidationException(
                    "For: " + Params.FORMID + "\nExpected: " + existingFormConfig.getFormid() + 
                            "\n   Found: " + receivedViaRequest.getFormid() + 
                            "\nExpected: " + existingFormConfig + "\n   Found: " + receivedViaRequest);
        }
    }
    
    public boolean updateParentForm(FormConfigStore store, FormRequest formRequest) {
        return this.parentFormUpdater.updateParent(store, formRequest);
    }
    
    /**
     * Form ids need to be unique within a session.
     * @return 
     */
    public String generateFormId() {
        return FORM_ID_PREFIX + Long.toHexString(System.currentTimeMillis());
    }
}
