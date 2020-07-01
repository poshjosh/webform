package com.looseboxes.webform.controllers;

import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.services.FormService;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.services.FormAttributeService;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigBean;
import com.looseboxes.webform.util.Print;
import com.looseboxes.webform.web.FormRequest;
import com.looseboxes.webform.web.WebformHttpServletRequest;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.request.WebRequest;

/**
 * @author hp
 * @param <T> The type of the model object
 */
//@see https://stackoverflow.com/questions/30616051/how-to-post-generic-objects-to-a-spring-controller
@SessionAttributes({HttpSessionAttributes.MODELOBJECT}) 
public class FormControllerBase<T>{
    
    private final Logger log = LoggerFactory.getLogger(FormControllerBase.class);
    
    @Autowired private FormService<T> service;
    @Autowired private FormAttributeService formAttributeService;

    public FormControllerBase() { }

    public FormConfigBean onBeginForm(
            ModelMap model, FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response){
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        this.log("showForm", model, null, formConfig, request, response);
        
        final FormRequest formRequest = this.getFormRequest(model, formConfig, request);
        
        formConfig = this.service.onBeginForm(formRequest).getFormConfig();

        return formConfig;
    }
    
    public FormConfigBean onValidateForm(
            T modelobject, BindingResult bindingResult,
            ModelMap model, FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);

        this.log(FormStage.validate, model, modelobject, formConfig, request, response);
        
        final FormRequest formRequest = this.getFormRequest(model, formConfig, request);
        
        formConfig = this.service.onValidateForm(
                modelobject, bindingResult, formRequest).getFormConfig();

        return formConfig;
    }    

    public FormConfigBean onSubmitForm(
            ModelMap model, FormConfigBean formConfig,
            HttpServletRequest request, HttpServletResponse response) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        this.log(FormStage.submit, model, null, formConfig, request, response);
        
        final FormRequest formRequest = this.getFormRequest(model, formConfig, request);
        
        formConfig = this.service.onSubmitForm(formRequest).getFormConfig();
        
        return formConfig;
    } 

    public Map<String, Map> onGetDependents(
            ModelMap model, String formid, 
            String propertyName, String propertyValue,
            HttpServletRequest request, HttpServletResponse response) {

        log.debug("#onGetDependents {} = {}, session: {}", 
                propertyName, propertyValue, request.getSession().getId());

        final FormConfigBean formConfig = this.findFormConfig(request, formid);

        this.log(FormStage.dependents, model, null, formConfig, request, response);

        return this.service.dependents(model, formConfig, 
                propertyName, propertyValue, request.getLocale());
    }
    
    public FormConfig onValidateSingle(
            T modelobject, BindingResult bindingResult, ModelMap model, 
            String formid, String propertyName, String propertyValue,
            HttpServletRequest request, HttpServletResponse response) {
            
        log.debug("#onValidateSingle {} = {}, session: {}", 
                propertyName, propertyValue, request.getSession().getId());

        final FormConfigBean formConfig = this.findFormConfig(request, formid);

        this.log(FormStage.validateSingle, 
                model, modelobject, formConfig, request, response);
        
        return this.service.validateSingle(modelobject, bindingResult, 
                model, formConfig, propertyName, propertyValue);
    }
    

    /**
     * Return a valid {@link com.looseboxes.webform.web.FormConfigBean} for
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
    public FormConfigBean findFormConfig(HttpServletRequest request, String formid) {
        final FormConfigBean formConfig = 
                (FormConfigBean)request.getSession().getAttribute(formid);
        return this.checkNotNull(formConfig);
    }

    /**
     * @see #findFormConfig(javax.servlet.http.HttpServletRequest, java.lang.String) 
     * @param request
     * @param formid
     * @return 
     */
    public FormConfigBean findFormConfig(WebRequest request, String formid) {
        final FormConfigBean formConfig = 
                (FormConfigBean)request.getAttribute(formid, WebRequest.SCOPE_SESSION);
        return this.checkNotNull(formConfig);
    }
    
    private FormConfigBean checkNotNull(FormConfigBean formConfig) {
        log.trace("Found: {}", formConfig);
        if(formConfig == null) {
            throw new InvalidRouteException();
        }
        return formConfig;
    }

    public Optional<String> getTargetAfterSubmit(FormConfig formConfig) {
        final String targetOnCompletion = formConfig.getTargetOnCompletion();
        return targetOnCompletion == null ? Optional.empty() : 
                Optional.of("redirect:" + targetOnCompletion);
    }    
    

    public FormRequest getFormRequest(
            ModelMap model, FormConfigBean formConfig, HttpServletRequest request) {
        final FormRequest formRequest = new WebformHttpServletRequest(
                request, model, this.formAttributeService).formConfig(formConfig);
        return formRequest;
    }
    
    protected void log(String id, ModelMap model, 
            Object modelobject, FormConfig formConfig,
            HttpServletRequest request, HttpServletResponse response){
        
        final Object existing = formConfig.getModelobject();
        final Object sessionAttr = request.getSession().getAttribute(HttpSessionAttributes.MODELOBJECT);
        System.out.println("MOC - " + id + ", model name: " + formConfig.getModelname() + ", formid: " + 
                formConfig.getFid() + ", parent formid: " + formConfig.getParentfid());
        System.out.println("MOC - Controller model object: " + modelobject);
        System.out.println("MOC - FormConfig model object: " + existing);
        System.out.println("MOC -    Session model object: " + sessionAttr);
        
        if(Print.isTraceEnabled()) {
            new Print().trace(id, model, formConfig, request, response);
        }
    }

    public FormService getService() {
        return service;
    }
}
