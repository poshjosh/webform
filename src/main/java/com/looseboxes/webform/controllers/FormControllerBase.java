package com.looseboxes.webform.controllers;

import com.looseboxes.webform.services.FormControllerService;
import com.bc.webform.Form;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.form.UpdateParentFormWithNewlyCreatedModel;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.FormConfigBean;
import com.looseboxes.webform.services.FormService;
import com.looseboxes.webform.services.WebRequestFileUploadConfig;
import com.looseboxes.webform.store.StoreDelegate;
import com.looseboxes.webform.util.Print;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * @author hp
 */
//@see https://stackoverflow.com/questions/30616051/how-to-post-generic-objects-to-a-spring-controller
@SessionAttributes({HttpSessionAttributes.MODELOBJECT}) 
public class FormControllerBase{
    
    private final Logger log = LoggerFactory.getLogger(FormControllerBase.class);
    
    @Autowired private FormService _genericFormSvc;    
    @Autowired private FormControllerService service;
    @Autowired private UpdateParentFormWithNewlyCreatedModel parentFormUpdater;

    public FormControllerBase() { }

    public FormConfig onBeginForm(
            ModelMap model, FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response){
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        this.log("showForm", model, formConfig, request, response);
        
        final FormService formSvc = this.getFormService(model, request);
        
        formConfig = this.service.onBeginForm(model, formConfig, formSvc);
        
        return formConfig;
    }
    
    public FormConfig onValidateForm(
            Object modelobject, BindingResult bindingResult,
            ModelMap model, FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);

        this.log("validateForm", model, formConfig, request, response);
        
        final FormService formSvc = this.getFormService(model, request);
        
        formConfig = this.service.onValidateForm(
                modelobject, bindingResult, model, formConfig, formSvc, 
                WebRequestFileUploadConfig.validInstanceOrNull(request));

        return formConfig;
    }    

    public FormConfig onSubmitForm(
            ModelMap model, FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        this.log("submitForm", model, formConfig, request, response);
        
        final FormService formSvc = this.getFormService(model, request);
        
        formConfig = this.service.onSubmitForm(model, formConfig, formSvc);

        this.updateParentForm(formConfig, formSvc);
        
        return formConfig;
    } 
    
    private void updateParentForm(FormConfig formConfig, FormService formSvc) {
    
        if(CRUDAction.create.equals(formConfig.getCrudAction())) {

            final Form<Object> form = Objects.requireNonNull(formConfig.getForm());

            final Form<Object> parentForm = form.getParent();

            if(parentForm == null) {
                
                log.debug("No parent to update for form: {}", form);
                
            }else{
            
                final String parentFormId = parentForm.getId();
            
                FormConfig parentFormConfig = formSvc.getFormAttributeService()
                        .getSessionAttributeOrException(parentFormId);

                if(parentFormConfig != null) {

                    try{

                        parentFormConfig = this.parentFormUpdater
                                .updateParent(formConfig, parentFormConfig);

                        formSvc.getFormAttributeService().setSessionAttribute(parentFormConfig);

                    }catch(RuntimeException e) {
                        log.warn("Failed to update parent with this form's value", e);
                    }
                }
            }
        }
    }

    public Optional<String> getTargetAfterSubmit(FormConfig formConfig) {
        final String targetOnCompletion = formConfig.getTargetOnCompletion();
        return targetOnCompletion == null ? Optional.empty() : 
                Optional.of("redirect:" + targetOnCompletion);
    }    

    public FormService getFormService(ModelMap model, HttpServletRequest request) {
        
        final FormService formSvc = this._genericFormSvc.wrap(
                this.getStoreDelegate(model, request));
        
        return formSvc;
    } 
    
    private StoreDelegate getStoreDelegate(ModelMap model, HttpServletRequest request) {
        return new StoreDelegate(model, request);
    }
    
    protected void log(String id, ModelMap model, FormConfigBean formConfigDTO,
            HttpServletRequest request, HttpServletResponse response){
        if(log.isTraceEnabled()) {
            new Print().trace(id, model, formConfigDTO, request, response);
        }
    }

    public FormControllerService getService() {
        return service;
    }
}
/**
 * 
    
    public void validateModelObject(
            BindingResult bindingResult, ModelMap model, FormConfig formConfig) {
        
        final Object modelobject = Objects.requireNonNull(formConfig.getModelobject());
        
        this.validateModelObject(bindingResult, model, formConfig, modelobject);
    }

    public void validateModelObject(
            BindingResult bindingResult, ModelMap model,
            FormConfig formConfig, Object modelobject) {
        
        Objects.requireNonNull(bindingResult);
        Objects.requireNonNull(model);
        Objects.requireNonNull(formConfig);
        Objects.requireNonNull(modelobject);
    
        final List<Validator> validators = this.formValidatorFactory
                .getValidators(formConfig, modelobject.getClass());

        for(Validator validator : validators) {

            ValidationUtils.invokeValidator(validator, modelobject, bindingResult);
        }
        
        if (bindingResult.hasErrors()) {
            
            this.messageAttributesSvc.addErrorsToModel(bindingResult, model);
        }
    }
    
    public AttributeService getAttributeService(
            ModelMap model, HttpServletRequest request) {
    
        return this._genericAttributeSvc.wrap(getStoreDelegate(model, request));
    }
    
    public UpdateParentFormWithNewlyCreatedModel getFormUpdaterPostCreate(
            ModelMap model, HttpServletRequest request) {
        return _genericFormUpdaterPostCreate.wrap(getStoreDelegate(model, request));
    }
    
    protected FormService getGenericFormSvc() {
        return _genericFormSvc;
    }

    public FormValidatorFactory getFormValidatorFactory() {
        return formValidatorFactory;
    }

    protected AttributeService getGenericAttributeSvc() {
        return _genericAttributeSvc;
    }

    public MessageAttributesService getMessageAttributesSvc() {
        return messageAttributesSvc;
    }

    public FileUploadService getFileUploadSvc() {
        return fileUploadSvc;
    }

    public FormSubmitHandler getFormSubmitHandler() {
        return formSubmitHandler;
    }

 * 
 */