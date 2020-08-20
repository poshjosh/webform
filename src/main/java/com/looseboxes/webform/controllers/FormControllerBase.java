package com.looseboxes.webform.controllers;

import com.bc.webform.choices.SelectOption;
import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.services.FormService;
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
import com.looseboxes.webform.store.AttributeStore;
import com.looseboxes.webform.store.AttributeStoreProvider;
import com.looseboxes.webform.store.FormConfigStore;
import java.util.List;
import java.util.Optional;
import javax.cache.Cache;

/**
 * @author hp
 * @param <T> The type of the model object
 */
public class FormControllerBase<T>{
    
    private final Logger log = LoggerFactory.getLogger(FormControllerBase.class);
    
    @Autowired private AttributeStoreProvider storeProvider;
    @Autowired private FormService<T> formService;
    @Autowired private WebformEventPublisher eventPublisher;

    public FormControllerBase() { }
    
    public FormConfigDTO onBeginForm(FormConfigDTO formConfig, HttpServletRequest request){
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        final FormRequest formRequest = this.getFormRequest(formConfig, request);
        
        this.publishStageBegunAndLog(formRequest, FormStage.BEGIN, request);
        
        final FormConfigStore store = this.getStore(request);
        
        formConfig = this.formService.onBeginForm(store, formRequest).getFormConfig();
        
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
        
        final FormConfigStore store = this.getStore(request);
        
        formConfig = this.formService.onValidateForm(store, formRequest, webRequest).getFormConfig();
        
        this.publishStageCompletedAndLog(formRequest, FormStage.BEGIN, request);
        
        return formConfig;
    }    

    public FormConfigDTO onSubmitForm(
            FormConfigDTO formConfig, HttpServletRequest request) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        final FormRequest formRequest = this.getFormRequest(formConfig, request);
        
        this.publishStageBegunAndLog(formRequest, FormStage.SUBMIT, request);
        
        final FormConfigStore store = this.getStore(request);
        
        formConfig = this.formService.onSubmitForm(store, formRequest).getFormConfig();
        
        this.publishStageCompletedAndLog(formRequest, FormStage.BEGIN, request);
        
        return formConfig;
    } 

    public Map<String, List<SelectOption>> onGetDependents(String formid, 
            String propertyName, String propertyValue,
            HttpServletRequest request) {

        log.debug("#onGetDependents {} = {}, session: {}", 
                propertyName, propertyValue, request.getSession().getId());

        final FormConfigStore store = this.getStore(request);
        
        final Map<String, List<SelectOption>> dependents = formService
                .dependents(store, formid, propertyName, propertyValue, request.getLocale());
        
        return dependents;
    }
    
    public FormConfigDTO onValidateSingle(String formid, 
            String propertyName, String propertyValue,
            HttpServletRequest request) {
            
        log.debug("#onValidateSingle {} = {}, session: {}", 
                propertyName, propertyValue, request.getSession().getId());

        FormConfigStore store = this.getStore(request);
        
        return this.formService.validateSingle(store, formid, propertyName, propertyValue);
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

    public FormConfigStore getStore(HttpServletRequest request) {
        return new FormConfigStore(this.getAttributeStore(request));
    }
        
    public AttributeStore getAttributeStore(HttpServletRequest request) {
        return getCacheStore().orElse(getSessionStore(request));
    }
    
    private Optional<AttributeStore> getCacheStore() {
        return this.getCache().map((cache) -> storeProvider.forCache(cache));
    }
    
    private AttributeStore getSessionStore(HttpServletRequest request) {
        return storeProvider.forSession(request.getSession());
    }

    public Optional<Cache> getCache() {
        return Optional.empty();
    }

    public FormRequest getFormRequest(FormConfigDTO formConfig, HttpServletRequest request) {
        final FormRequest formRequest = new WebformHttpServletRequest(request).formConfig(formConfig);
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
