package com.looseboxes.webform.controllers;

import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.Params;
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
    
    @Autowired private FormService _genericFormSvc;
    @Autowired private FormValidatorFactory formValidatorFactory;
    @Autowired private AttributeService _genericAttributeSvc;
    @Autowired private MessageAttributesService messageAttributesSvc;
    @Autowired private FileUploadService fileUploadSvc;
    @Autowired private OnFormSubmitted onFormSubmitted;
    @Autowired private UpdateParentFormWithNewlyCreatedModel _genericFormUpdaterPostCreate;

    public FormControllerBase() { }

    public FormConfig onBeginForm(ModelMap model, FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response){
        
        this.updateFormConfigWithRequestParameters(formConfig, request);
        
        this.log("showForm", model, formConfig, request, response);
        
        final FormService formSvc = getFormService(model, request);
        
        formConfig = formSvc.onShowform(formConfig);
        
        log.debug("{}", formConfig);
        
        //////////////////////////// IMPORTANT NOTE ////////////////////////////
        // When we didn't clear this model, the session was getting re-populated
        // by the modelobject even after the modelobject had been removed from
        // the session.
        // Two attributes were identified in the ModelMap: formConfigBean and 
        // org.springframework.validation.BindingResult.formConfigBean. 
        ////////////////////////////////////////////////////////////////////////
        // 
        model.clear();
        model.putAll(formConfig.toMap());
        
        formSvc.attributeService().setSessionAttribute(formConfig);
        request.getSession().setAttribute(
                HttpSessionAttributes.MODELOBJECT, formConfig.getModelobject());
        if(log.isTraceEnabled()) {
            new Print().printHttpSession(request.getSession())
                    .print("FormConfig", formConfig);
        }
        
        return formConfig;
    }
    
    public FormConfig onValidateForm(
            Object modelobject,
            BindingResult bindingResult,
            ModelMap model,
            FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response) {
        
        this.updateFormConfigWithRequestParameters(formConfig, request);

        this.log("validateForm", model, formConfig, request, response);
        
        final FormService formSvc = getFormService(model, request);
        
        formConfig = formSvc.onValidateForm(formConfig, modelobject);

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
    
        final List<Validator> validators = this.formValidatorFactory
                .getValidators(formConfig, modelobject.getClass());

        for(Validator validator : validators) {

            ValidationUtils.invokeValidator(validator, modelobject, bindingResult);
        }
        
        if (bindingResult.hasErrors()) {
            
            this.messageAttributesSvc.addErrorsToModel(bindingResult, model);
        }
    }

    public FormConfig onSubmitForm(
            ModelMap model,
            FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response) {
        
        this.updateFormConfigWithRequestParameters(formConfig, request);
        
        this.log("submitForm", model, formConfig, request, response);
        
        final FormService formSvc = getFormService(model, request);
        
        formConfig = formSvc.onSubmitForm(formConfig);
        
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
                    this.getFormUpdaterPostCreate(model, request).updateParent(formConfig);
                }catch(RuntimeException e) {
                    log.warn("Failed to update parent with this form's value", e);
                }
            }
        }catch(RuntimeException e) {

            attributeSvc.deleteUploadedFiles();
            
            this.onFormSubmitFailed(model, formConfig, e, request, response);
            
        }finally{
            
            formSvc.attributeService().removeSessionAttribute(formConfig.getFormid());
//            attributeSvc.sessionAttributes().remove(HttpSessionAttributes.MODELOBJECT);
            attributeSvc.remove(HttpSessionAttributes.MODELOBJECT);
            attributeSvc.removeAll(FormConfig.names());
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
    
    /**
     * Update the FormConfigBean with form related parameters from the request.
     * 
     * The FormConfigBean passed by Spring to the controller methods was not
     * being updated with parameters from query e.g <code>?user=jane&age=23</code>
     * 
     * This method manually updates those parameters in the FormConfigBean
     * @param formConfig
     * @param request 
     */
    public void updateFormConfigWithRequestParameters(
            FormConfigBean formConfig, HttpServletRequest request) {
        log.trace("BEFORE: {}\nHttpServletRequest.queryString: {}", 
                formConfig, request.getQueryString());
        final String [] names = Params.names();
        for(String name : names) {
            final Object value = this.getParameter(request, name);
            if(value != null) {
                formConfig.setIfAbsent(name, value);
            }
        }
        log.debug(" AFTER: {}", formConfig);
    }

    /**
     * Update the ModelMap with form related parameters from the request.
     * This method manually updates those parameters in the ModelMap
     * @param model
     * @param request 
     */
    public void updateModelMapWithRequestParameters(
            ModelMap model, HttpServletRequest request) {
        log.trace("BEFORE: {}\nHttpServletRequest.queryString: {}", 
                model, request.getQueryString());
        final String [] names = Params.names();
        for(String name : names) {
            final Object value = this.getParameter(request, name);
            if(value != null) {
                model.putIfAbsent(name, value);
            }
        }
        log.debug(" AFTER: {}", model);
    }
    
    private Object getParameter(HttpServletRequest request, String name) {
        final Object value;
        if(Params.isMultiValue(name)) {
            value = request.getParameterValues(name);
        }else{
            value = this.getSingleParameterOrNull(request, name);
        }
        log.trace("HttpServletRequest parameter: {} = {}", name, value);
        return value;
    }
    
    private String getSingleParameterOrNull(HttpServletRequest request, String name){
        final String param = request.getParameter(name);
        return param == null || param.isEmpty() ? null : param;
    }
    
    public AttributeService getAttributeService(
            ModelMap model, HttpServletRequest request) {
    
        return this._genericAttributeSvc.wrap(getStoreDelegate(model, request));
    }
    
    public FormService getFormService(ModelMap model, HttpServletRequest request) {
        
        final FormService formSvc = this._genericFormSvc.wrap(
                this.getStoreDelegate(model, request));
        
        return formSvc;
    } 
    
    public UpdateParentFormWithNewlyCreatedModel getFormUpdaterPostCreate(
            ModelMap model, HttpServletRequest request) {
        return _genericFormUpdaterPostCreate.wrap(getStoreDelegate(model, request));
    }
    
    public StoreDelegate getStoreDelegate(ModelMap model, HttpServletRequest request) {
        return new StoreDelegate(model, request);
    }
    
    protected void log(String id, ModelMap model, FormConfigBean formConfigDTO,
            HttpServletRequest request, HttpServletResponse response){
        if(log.isTraceEnabled()) {
            new Print().trace(id, model, formConfigDTO, request, response);
        }
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

    public OnFormSubmitted getOnFormSubmitted() {
        return onFormSubmitted;
    }
}
