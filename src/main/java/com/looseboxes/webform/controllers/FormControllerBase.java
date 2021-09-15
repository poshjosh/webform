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

    public FormConfigDTO onBeginThenValidateThenSubmitForm(FormConfigDTO formConfig,
            HttpServletRequest request, WebRequest webRequest) {
        
        formConfig = this.onBeginForm(formConfig, request);
        
        if( ! formConfig.hasErrors()) {
        
            formConfig = this.onValidateThenSubmitForm(formConfig, request, webRequest);
        }

        return formConfig;
    }
    
    public FormConfigDTO onBeginForm(FormConfigDTO formConfig, HttpServletRequest request){
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        final FormConfigStore store = this.getStore(request);

        final FormRequest formRequest = this.getFormRequest(formConfig, request);
        
        this.publishStageBegunAndLog(store, formRequest, FormStage.BEGIN, request);
        
        formConfig = this.formService.onBeginForm(store, formRequest).getFormConfig();
        
        this.publishStageCompletedAndLog(store, formRequest, FormStage.BEGIN, request);

        return formConfig;
    }
    
    public FormConfigDTO onValidateThenSubmitForm(FormConfigDTO formConfig,
            HttpServletRequest request, WebRequest webRequest) {
        
        formConfig = this.onValidateForm(formConfig, request, webRequest);
        
        if( ! formConfig.hasErrors()) {
        
            formConfig = this.onSubmitForm(formConfig, request);
        }
        
        return formConfig;
    }
    
    public FormConfigDTO onValidateForm(FormConfigDTO formConfig,
            HttpServletRequest request, WebRequest webRequest) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);

        FormRequest formRequest = this.getFormRequest(formConfig, request);
        
        final FormConfigStore store = this.getStore(request);

        this.publishStageBegunAndLog(store, formRequest, FormStage.VALIDATE, request);
       
        formConfig = this.formService.onValidateForm(store, formRequest, webRequest).getFormConfig();
        
        this.publishStageCompletedAndLog(store, formRequest, FormStage.VALIDATE, request);
        
        return formConfig;
    }    

    public FormConfigDTO onSubmitForm(
            FormConfigDTO formConfig, HttpServletRequest request) {
        
        FormParamsUtil.updateFormConfigWithFormParamsFromRequest(formConfig, request);
        
        final FormConfigStore store = this.getStore(request);
        
        final FormRequest formRequest = this.getFormRequest(formConfig, request);
        
        this.publishStageBegunAndLog(store, formRequest, FormStage.SUBMIT, request);
        
        formConfig = this.formService.onSubmitForm(store, formRequest).getFormConfig();
        
        this.publishStageCompletedAndLog(store, formRequest, FormStage.SUBMIT, request);
        
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
    
    private void publishStageBegunAndLog(
            FormConfigStore store, FormRequest formRequest, 
            FormStage stage, HttpServletRequest request) {
        
        formRequest.getFormConfig().setFormStage(stage);
    
        this.eventPublisher.publishFormStageBegunEvent(formRequest);

        this.log("BEGUN", store, formRequest, request);
    }
    
    private void publishStageCompletedAndLog(
            FormConfigStore store, FormRequest formRequest, 
            FormStage stage, HttpServletRequest request) {
        
        formRequest.getFormConfig().setFormStage(stage);

        this.eventPublisher.publishFormStageCompletedEvent(formRequest);
        
        this.log("ENDED", store, formRequest, request);
    }

    public FormConfigStore getStore(HttpServletRequest request) {
        return new FormConfigStore(this.getAttributeStore(request));
    }
        
    public AttributeStore getAttributeStore(HttpServletRequest request) {
        return getCacheStore().orElseGet(() -> getSessionStore(request));
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
    
    private void log(String tag, 
            FormConfigStore store, FormRequest formRequest, HttpServletRequest request){
        
        FormConfigDTO formConfig = formRequest.getFormConfig();
        
        final FormStage formStage = formConfig == null ? null : formConfig.getFormStage();
        
        if(Print.isTraceEnabled()) {
            new Print().trace(formStage, null, request);
        }
        if(log.isTraceEnabled()) {
            log.trace("{} Current session ID: {}", tag, request.getSession().getId());
            log.trace("RECEIVED FormConfig");
            log.trace("{}", formConfig == null ? null : formConfig.print());
            log.trace("EXISTING FormConfig");
            final String formId = formConfig == null ? null : formConfig.getFormid();
            FormConfigDTO existing = formId == null ? null : store.getOrDefault(formId, null);
            log.trace("{}", existing == null ? null : existing.print());
        }
    }
    
    public FormService getService() {
        return formService;
    }
}
