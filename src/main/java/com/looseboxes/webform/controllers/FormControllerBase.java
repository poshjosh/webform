package com.looseboxes.webform.controllers;

import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.FormConfigBean;
import com.looseboxes.webform.form.UpdateParentFormWithNewlyCreatedModel;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.services.AttributeService;
import com.looseboxes.webform.services.FileUploadService;
import com.looseboxes.webform.services.FormService;
import com.looseboxes.webform.services.MessageAttributesService;
import com.looseboxes.webform.store.StoreDelegate;
import com.looseboxes.webform.util.Print;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author hp
 */
//@see https://stackoverflow.com/questions/30616051/how-to-post-generic-objects-to-a-spring-controller
@SessionAttributes({HttpSessionAttributes.MODELOBJECT}) 
public class FormControllerBase{
    
    private final Logger log = LoggerFactory.getLogger(FormControllerBase.class);
    
    @FunctionalInterface
    public static interface OnFormSubmitted{
        void onFormSubmitted(FormConfig formReqParams);
    }
    
    @Autowired private FormService genericFormSvc;
    @Autowired private FormValidatorFactory formValidatorFactory;
    @Autowired private AttributeService genericAttributeSvc;
    @Autowired private MessageAttributesService messageAttributesSvc;
    @Autowired private FileUploadService fileUploadSvc;
    @Autowired private OnFormSubmitted onFormSubmitted;
    @Autowired private UpdateParentFormWithNewlyCreatedModel updateParentFormWithNewlyCreatedModel;

    public FormControllerBase() { }

    public FormConfig onBeginForm(ModelMap model, FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response){
        
        this.log("showForm", model, formConfigBean, request, response);
        
        final FormService formSvc = getFormService(model, request);
        
        final FormConfig formConfig = formSvc.onShowform(formConfigBean);
        formConfigBean = null; // Prevent usage
        
        log.debug("{}", formConfig);
        
        final AttributeService attributeSvc = this.getAttributeService(model, request);
        
        attributeSvc.modelAttributes().putAll(formConfig.toMap());
        
        formSvc.attributeService().setSessionAttribute(formConfig);
        
        return formConfig;
    }
    
    public FormConfig onValidateForm(
            Object modelobject,
            BindingResult bindingResult,
            ModelMap model,
            FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response) {
        
        this.log("validateForm", model, formConfigBean, request, response);
        
        final FormService formSvc = getFormService(model, request);
        
        final FormConfig formConfig = formSvc.onValidateForm(formConfigBean, modelobject);
        formConfigBean = null; // Prevent usage

        log.debug("{}", formConfig);
        
        this.validateModelObject(bindingResult, model, formConfig);
        
        formSvc.checkAll(formConfig);
  
        final Map<String, Object> formParams = formConfig.toMap();
        
        final AttributeService attributeSvc = getAttributeService(model, request);

        attributeSvc.modelAttributes().putAll(formParams);
        
        if ( ! bindingResult.hasErrors()) {
            
            if(request instanceof MultipartHttpServletRequest) {
            
                final Collection<String> uploadedFiles = fileUploadSvc.upload(
                        formConfig.getModelname(), 
                        modelobject, 
                        (MultipartHttpServletRequest)request);

                attributeSvc.addUploadedFiles(uploadedFiles);
            }
        }

        return formConfig;
    }    
    
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
    
        final List<Validator> validators = this.formValidatorFactory.get(formConfig);

        for(Validator validator : validators) {

            ValidationUtils.invokeValidator(validator, modelobject, bindingResult);
        }
        
        if (bindingResult.hasErrors()) {
            
            this.messageAttributesSvc.addErrorsToModel(bindingResult, model);
        }
    }

    public FormConfig onSubmitForm(
            ModelMap model,
            FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response) {
        
        this.log("submitForm", model, formConfigBean, request, response);
        
        final FormService formSvc = getFormService(model, request);
        
        final FormConfig formConfig = formSvc.onSubmitForm(formConfigBean);
        formConfigBean = null; // Prevent usage
        
        log.debug("{}", formConfig);

        formSvc.checkAll(formConfig);

        final AttributeService attributeSvc = getAttributeService(model, request);
        
        try{
        
            this.onFormSubmitted.onFormSubmitted(formConfig);
            
            attributeSvc.removeUploadedFiles(null);
            
            //@Todo periodic job to check uploads dir for orphan files and deleteManagedEntity

            this.onFormSubmitSuccessful(model, formConfig, request, response);
            
            if(CRUDAction.create.equals(formConfig.getCrudAction())) {
                try{
                    updateParentFormWithNewlyCreatedModel.updateParent(formConfig);
                }catch(RuntimeException e) {
                    log.warn("Failed to update parent with this form's value", e);
                }
            }
        }catch(RuntimeException e) {

            attributeSvc.deleteUploadedFiles();
            
            this.onFormSubmitFailed(model, formConfig, e, request, response);
            
        }finally{
            
            formSvc.attributeService().removeSessionAttribute(formConfig.getFormid());
            attributeSvc.sessionAttributes().remove(HttpSessionAttributes.MODELOBJECT);
        }
        
        return formConfig;
    } 

    public void onFormSubmitSuccessful(
            ModelMap model, FormConfig formConfig,
            HttpServletRequest request, HttpServletResponse response) {

        log.debug("SUCCESS: {}", formConfig);
            
        final Object m = "Successfully completed action: " + 
                formConfig.getCrudAction() + ' ' + formConfig.getModelname();
        
        this.messageAttributesSvc.addInfoMessage(model, m);
    }
    
    public void onFormSubmitFailed(
            ModelMap model, FormConfig formConfig, Exception exception,
            HttpServletRequest request, HttpServletResponse response) {
    
        log.warn("Failed to process: " + formConfig, exception);

        this.messageAttributesSvc.addErrorMessages(model, 
                "Unexpected error occured while processing action: " + 
                        formConfig.getCrudAction() + ' ' + formConfig.getModelname());
    }

    public Optional<String> getTargetAfterSubmit(FormConfig formConfig) {
        final String targetOnCompletion = formConfig.getTargetOnCompletion();
        return targetOnCompletion == null ? Optional.empty() : 
                Optional.of("redirect:" + targetOnCompletion);
    }    
    
    public AttributeService getAttributeService(
            ModelMap model, HttpServletRequest request) {
    
        final StoreDelegate delegate = new StoreDelegate(model, request);
        
        return this.genericAttributeSvc.wrap(delegate);
    }
    
    public FormService getFormService(ModelMap model, HttpServletRequest request) {
        
        final StoreDelegate delegate = new StoreDelegate(model, request);
        
        final FormService formSvc = this.genericFormSvc.wrap(delegate);
        
        return formSvc;
    } 
    
    protected void log(String id, ModelMap model, FormConfigBean formConfigDTO,
            HttpServletRequest request, HttpServletResponse response){
        if(log.isTraceEnabled()) {
            new Print().trace(id, model, formConfigDTO, request, response);
        }
    }
    
    protected FormService getGenericFormSvc() {
        return genericFormSvc;
    }

    public FormValidatorFactory getFormValidatorFactory() {
        return formValidatorFactory;
    }

    public AttributeService getGenericAttributeSvc() {
        return genericAttributeSvc;
    }

    public MessageAttributesService getMessageAttributesSvc() {
        return messageAttributesSvc;
    }

    public FileUploadService getFileUploadSvc() {
        return fileUploadSvc;
    }

    public OnFormSubmitted getOnFormSubmitted() {
        return onFormSubmitted;
    }
}
