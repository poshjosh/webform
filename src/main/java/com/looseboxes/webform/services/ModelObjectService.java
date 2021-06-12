package com.looseboxes.webform.services;

import com.bc.webform.form.Form;
import com.bc.webform.form.FormBean;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.web.FormConfigDTO;
import org.springframework.lang.Nullable;
import com.looseboxes.webform.configurers.EntityConfigurerService;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.form.util.UpdateParentFormWithNewlyCreatedModel;
import com.looseboxes.webform.store.FormConfigStore;
import com.looseboxes.webform.web.FormRequest;
import com.looseboxes.webform.web.WebstoreValidatingDataBinder;
import java.util.Collections;
import java.util.Optional;
import javax.validation.ValidationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.context.request.WebRequest;

/**
 * @author hp
 */
@Service
public class ModelObjectService{
    
    private final Logger log = LoggerFactory.getLogger(ModelObjectService.class);

    public static final String FORM_ID_PREFIX = "form";
    
    @Autowired private ModelObjectProvider modelObjectProvider;
    @Autowired private WebstoreValidatingDataBinder bindingValidator;
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
        
        return this.updateForm(store, formRequest, ! newForm, modelobject, null);
    }
    
    public <T> FormRequest<T> onValidateForm(FormConfigStore store, FormRequest<T> formRequest, WebRequest webRequest) {
        
        return this.updateForm(store, formRequest, true, null, webRequest);
    }
    
    public <T> FormRequest<T> onSubmitForm(FormConfigStore store, FormRequest<T> formRequest) {
        
        return this.updateForm(store, formRequest, true, null, null);
    }
    
    public <S, T> FormRequest<T> createNextFormConfigAndUpdateRequest(
            FormRequest<S> formRequest, String modelname, String modelid) {
        
        FormConfigDTO formConfig = formRequest.getFormConfig();
        
        String parentFormId = formConfig.getFormid();
        
        FormConfigDTO formConfigUpdate = formConfig.copy()
                .fid(this.generateFormId())
                // We set the form to null, because after the successfully
                // submitting a form we do not display the previous values
                //
                .form(null).id(modelid).modelfields(Collections.EMPTY_LIST)
                .modelname(modelname).parentfid(parentFormId)
                .targetOnCompletion(formConfig.getTargetOnCompletion());
        
//        formRequest.setFormConfig(formConfigUpdate);

//        return (FormRequest<T>)formRequest;

        return formRequest.copy().formConfig(formConfigUpdate);
    }
    
    private <T> FormRequest<T> updateForm(
            FormConfigStore store, FormRequest<T> formRequest, 
            boolean existingForm, @Nullable T modelobject, @Nullable WebRequest webRequest) {
        
        FormConfigDTO formConfig = formRequest.getFormConfig();
        
        final CRUDAction action = formConfig.getCrudAction();
        final String formid = formConfig.getFormid();
        final String modelname = formConfig.getModelname();
        final String modelid = formConfig.getModelid();
        final String parentFormId = formConfig.getParentFormid();
        
        if(CRUDAction.create != action && modelid == null) {
            throw new AttributeNotFoundException(modelname, Params.MODELID);
        }
        
        FormConfigDTO existingFormConfig = getExistingFormConfig(store, formid, existingForm).orElse(null);
        
        log.trace("Received model: {}\nExisting model: {}", formConfig.getModelobject(), 
                (existingFormConfig==null?null:existingFormConfig.getModelobject()));

        log.debug("Received form: {}\nExisting form: {}", 
                formConfig.getForm(),
                (existingFormConfig==null?null:existingFormConfig.getForm()));
        
        log.trace("Received form config: {}\nExisting form config: {}", formConfig, existingFormConfig);

        if(existingForm && existingFormConfig == null) {
            throw new InvalidRouteException();
        }

        final Form parentForm = parentFormId == null ? null : 
                store.getFormOrException(parentFormId);
        
        if (modelobject == null) {
            modelobject = (T)existingFormConfig.getModelobject();
//            modelobject = (T)formConfig.getModelobject();
        }
        
        final BindingResult bindingResult = this.bind(webRequest, modelobject);

        if(modelobject != null && this.shouldConfigureModelObject(formRequest)) {
            modelobject = this.configureModelObject(modelobject, formRequest);
        }
        
        FormBean form = (FormBean)this.formFactory.newForm(parentForm, formid, modelname, modelobject);
//        FormBean form = formConfig.getForm();
//        if(form == null) {
//            form = (FormBean)this.formFactory.newForm(parentForm, formid, modelname, modelobject);
//            formConfig.setForm(form);
//        }else{
//            form.setParent(parentForm);
//            form.setDataSource(modelobject);
//        }

        if(existingFormConfig == null) {
            
            formRequest.setFormConfig(formConfig.form(form));

        }else{
  
            this.validateFormId(existingFormConfig, formConfig);
            
            formConfig.merge(existingFormConfig);
            
            formRequest.setFormConfig(formConfig.form(form));
        }
        
        if(bindingResult != null) {
        
            formRequest.getFormConfig().setBindingResult(bindingResult);
        }
        
        return formRequest;
    }   
    
    public BindingResult bind(WebRequest webRequest, Object modelobject) {
        
        final boolean validate = webRequest != null;
        
        final BindingResult bindingResult;
        
        if(validate) {
        
            final DataBinder dataBinder = bindingValidator.bind(webRequest, modelobject);

            bindingResult = bindingValidator.validate(dataBinder).getBindingResult();
            
        }else{
            
            bindingResult = null;
        }
        
        return bindingResult;
    }
    
    public Optional<FormConfigDTO> getExistingFormConfig(
            FormConfigStore store, String formid, boolean existingForm) {
        return Optional.ofNullable(
                ! existingForm ? store.getOrDefault(formid, null) : store.getOrException(formid)
        );
    }
    
    public boolean shouldConfigureModelObject(FormRequest formRequest) {
        final boolean configure;
        final CRUDAction crudAction = formRequest.getFormConfig().getCrudAction();
        switch(crudAction) {
            case create: configure = true; break;
            case read: configure = false; break;
            case update: configure = true; break;
            case delete: configure = false; break;
            default: throw Errors.unexpectedElement(crudAction, CRUDAction.values());
        }
        return configure;
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
        T result = entityConfigurerService.getConfigurer(type)
                        .map((configurer) -> configurer.configure(modelobject, formRequest))
                        .orElse(modelobject);
        log.trace("After configure: {}", result);
        return result;
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
