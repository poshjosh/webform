package com.looseboxes.webform.controllers;

import com.bc.webform.choices.SelectOption;
import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.services.FormService;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.services.FormAttributeService;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.util.Print;
import com.looseboxes.webform.web.FormRequest;
import com.looseboxes.webform.web.WebformHttpServletRequest;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.WebRequest;
import com.looseboxes.webform.events.WebformEventPublisher;
import java.util.List;

/**
 * @author hp
 * @param <T> The type of the model object
 */
public class FormControllerBase<T>{
    
    private final Logger log = LoggerFactory.getLogger(FormControllerBase.class);
    
    @Autowired private FormService<T> formService;
    @Autowired private FormAttributeService formAttributeService;
    @Autowired private WebformEventPublisher eventPublisher;

    public FormControllerBase() { }
    
    public FormConfigDTO onBeginForm(FormConfigDTO formConfig, HttpServletRequest request){
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        final FormRequest formRequest = this.getFormRequest(formConfig, request);
        
        this.publishStageBegunAndLog(formRequest, FormStage.BEGIN, request);
        
        formConfig = this.formService.onBeginForm(formRequest).getFormConfig();
        
        this.publishStageCompletedAndLog(formRequest, FormStage.BEGIN, request);

        return formConfig;
    }
    
    public FormConfigDTO onValidateThenSubmitForm(FormConfigDTO formConfig,
            HttpServletRequest request, WebRequest webRequest) {
        
        formConfig = this.onValidateForm(formConfig, request, webRequest);
        
        return this.onSubmitForm(formConfig, request);
    }
    
    public FormConfigDTO onValidateForm(FormConfigDTO formConfig,
            HttpServletRequest request, WebRequest webRequest) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);

        FormRequest formRequest = this.getFormRequest(formConfig, request);
        
        this.publishStageBegunAndLog(formRequest, FormStage.VALIDATE, request);
        
        formConfig = this.formService.onValidateForm(formRequest, webRequest).getFormConfig();
        
        this.publishStageCompletedAndLog(formRequest, FormStage.BEGIN, request);
        
        return formConfig;
    }    

    public FormConfigDTO onSubmitForm(
            FormConfigDTO formConfig, HttpServletRequest request) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        final FormRequest formRequest = this.getFormRequest(formConfig, request);
        
        this.publishStageBegunAndLog(formRequest, FormStage.SUBMIT, request);
        
        formConfig = this.formService.onSubmitForm(formRequest).getFormConfig();
        
        this.publishStageCompletedAndLog(formRequest, FormStage.BEGIN, request);
        
        return formConfig;
    } 

    public Map<String, List<SelectOption>> onGetDependents(String formid, 
            String propertyName, String propertyValue,
            HttpServletRequest request) {

        log.debug("#onGetDependents {} = {}, session: {}", 
                propertyName, propertyValue, request.getSession().getId());

        final FormConfigDTO formConfig = this.findFormConfig(request, formid);
        
        final Map<String, List<SelectOption>> dependents = formService.dependents(
                formConfig, propertyName, propertyValue, request.getLocale());
        
        return dependents;
    }
    
    public FormConfigDTO onValidateSingle(String formid, 
            String propertyName, String propertyValue,
            HttpServletRequest request) {
            
        log.debug("#onValidateSingle {} = {}, session: {}", 
                propertyName, propertyValue, request.getSession().getId());

        FormConfigDTO formConfig = this.findFormConfig(request, formid);
        
        this.formService.validateSingle(formConfig, propertyName, propertyValue);

        return formConfig;
    }
    
    protected void publishStageBegunAndLog(
            FormRequest formRequest, 
            FormStage stage, HttpServletRequest request) {
        
        formRequest.getFormConfig().setFormStage(stage);
    
        this.eventPublisher.publishFormStageBegunEvent(formRequest);

        this.log("BEGUN", stage, formRequest, request);
    }
    
    protected void publishStageCompletedAndLog(
            FormRequest formRequest, 
            FormStage stage, HttpServletRequest request) {
        
        formRequest.getFormConfig().setFormStage(stage);

        this.eventPublisher.publishFormStageCompletedEvent(formRequest);
        
        this.log("ENDED", stage, formRequest, request);
    }

    /**
     * Return a valid {@link com.looseboxes.webform.web.FormConfigDTO} for
     * the formid argument.
     * 
     * The formConfig passed via the request is not initialized with a 
     * form i.e FormConfig.getForm as well as FormConfig.getModelobject 
     * will return null. To check that the FormConig refers to a valid
     * instance, we use FormConfig.getFid() to search for a corresponding
     * session attribute; which should return an initialized instance
     * @param request
     * @param formid
     * @return 
     */
    public FormConfigDTO findFormConfig(HttpServletRequest request, String formid) {
        final FormConfigDTO formConfig = 
                (FormConfigDTO)request.getSession().getAttribute(formid);
        return this.checkNotNull(formConfig, "Attribute not found. Form having id: " + 
                formid + ", sessionId: " + request.getSession().getId());
    }

    /**
     * @see #findFormConfig(javax.servlet.http.HttpServletRequest, java.lang.String) 
     * @param request
     * @param formid
     * @return 
     */
    public FormConfigDTO findFormConfig(WebRequest request, String formid) {
        final FormConfigDTO formConfig = 
                (FormConfigDTO)request.getAttribute(formid, WebRequest.SCOPE_SESSION);
        return this.checkNotNull(formConfig, "Attribute not found. Form having id: " + 
                formid + ", sessionId: " + request.getSessionId());
    }
    
    private FormConfigDTO checkNotNull(FormConfigDTO formConfig, String message) {
        log.trace("Found: {}", formConfig);
        if(formConfig == null) {
            throw new InvalidRouteException(message);
        }
        return formConfig;
    }

    public FormRequest getFormRequest(FormConfigDTO formConfig, HttpServletRequest request) {
        final FormRequest formRequest = new WebformHttpServletRequest(
                request, this.formAttributeService).formConfig(formConfig);
        return formRequest;
    }
    
    protected void log(String tag, FormStage formStage, FormRequest formRequest, HttpServletRequest request){
        if(Print.isTraceEnabled()) {
            new Print().trace(formStage, formRequest.getFormConfig(), request);
        }else if(log.isDebugEnabled()){
            com.looseboxes.webform.controllers.Print.formAttributes(
                    tag + " form stage: " + formStage, request.getSession());
        }
    }

    public FormService getService() {
        return formService;
    }
}
