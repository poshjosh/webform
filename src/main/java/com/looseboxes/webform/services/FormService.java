package com.looseboxes.webform.services;

import com.looseboxes.webform.form.util.FileUploadHandler;
import com.bc.webform.choices.SelectOption;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.web.BindingResultErrorCollector;
import com.looseboxes.webform.form.util.DependentsProvider;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.form.util.FormSubmitHandler;
import com.looseboxes.webform.web.FormRequest;
import com.looseboxes.webform.web.WebstoreValidatingDataBinder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.WebRequest;
import com.looseboxes.webform.FormStages;
import com.looseboxes.webform.form.util.ModelObjectImagePathsProvider;
import com.looseboxes.webform.store.FormConfigStore;
import com.looseboxes.webform.util.StringUtils;
import java.util.List;
import java.util.Optional;

/**
 * @author hp
 */
@Service
public class FormService<T> {
    
    private final Logger log = LoggerFactory.getLogger(FormService.class);
    
    @Autowired private ModelObjectService modelObjectService;
    @Autowired private BindingResultErrorCollector bindingErrorCollector;
    @Autowired private DependentsProvider dependentsProvider;
    @Autowired private WebstoreValidatingDataBinder bindingValidator;
    @Autowired private FormValidatorService formValidatorService;
    @Autowired private ModelObjectImagePathsProvider imagePathsProvider;
    @Autowired(required = false) private FileUploadHandler fileUploadHandlerOptional;
    @Autowired private FormSubmitHandler formSubmitHandler;
    
    public FormRequest onBeginForm(FormConfigStore store, FormRequest formRequest){
        
        formRequest = modelObjectService.onBeginForm(store, formRequest);

        log.trace("{}", formRequest);

        final FormConfigDTO formConfig = formRequest.getFormConfig();
        store.set(formConfig);
        
        return formRequest;
    }

    public FormRequest onValidateForm(FormConfigStore store, FormRequest formRequest, WebRequest webRequest) {
        
        formRequest = modelObjectService.onValidateForm(store, formRequest, webRequest);
        
        log.trace("{}", formRequest);
        
        final FormConfigDTO formConfig = formRequest.getFormConfig();
        
        final BindingResult bindingResult = formConfig.getBindingResult();
        
        this.validateAndAddErrors(formConfig, bindingResult);
        
        this.check(formConfig);
        
        log.debug("Has errors: {}, Has files: {}", bindingResult.hasErrors(), formRequest.hasFiles());
  
        this.processFiles(formRequest);
        
        return formRequest;
    }    
    
    private void processFiles(FormRequest<Object> formRequest) {
        
        final FormConfigDTO formConfig = formRequest.getFormConfig();
        
        final BindingResult bindingResult = formConfig.getBindingResult();
        
        if ( ! bindingResult.hasErrors()) {

            Optional<FileUploadHandler> fileUploadHandlerOptional = this.getFileUploadService();
            
            if(CRUDAction.delete.equals(formConfig.getCrudAction())) {

                // We delete images in the root entity only
                // This is because, when we delete a product and the product
                // has a nested user, then we need not delete the product's user's images
                //
                if( ! imagePathsProvider.getImagePathsOfRootEntityOnly(formRequest).isEmpty()) {
                
                    if(fileUploadHandlerOptional.isPresent()) {
                        fileUploadHandlerOptional.get().deleteFilesOfRootEntityOnly(formRequest);
                    }else{
                        this.complainAboutFileUploadService();
                    }
                }
            }else if(formRequest.hasFiles()) {
                
                if(fileUploadHandlerOptional.isPresent()){
                
                    fileUploadHandlerOptional.get().upload(formRequest);
  
                }else{
                    
                    this.complainAboutFileUploadService();
                }
            }else{
                log.trace("There are no multi part files to process");
            }
        }
    }

    public FormRequest onSubmitForm(FormConfigStore store, FormRequest formRequest) {
        
        formRequest = modelObjectService.onSubmitForm(store, formRequest);
        
        log.trace("{}", formRequest);
        
        final FormConfigDTO formConfig = formRequest.getFormConfig();

        this.check(formConfig);

        try{
        
            this.formSubmitHandler.process(formRequest);
            
            if(formConfig.getModelobject() != null 
                    && formConfig.getParentFormOptional().isPresent()) {
                
                modelObjectService.updateParentForm(store, formRequest);
            }
        }catch(RuntimeException e) {

            if(formRequest.hasFiles()) {
                Optional<FileUploadHandler> optional = this.getFileUploadService();
                if(optional.isPresent()) {
                    optional.get().deleteUploadedFiles(formConfig);
                }else{
                    this.complainAboutFileUploadService();
                }
            }
            
            throw e;
            
        }finally{
            
            store.remove(formConfig.getFormid());
        }
        
        return formRequest;
    } 
    
    private void complainAboutFileUploadService() {
        String msg = ("Bean of type " + 
                FileUploadHandler.class.getName() + " is either not present or wrongly configured. This means that form images will not be processed");
        log.warn(msg);
    }

    public Map<String, List<SelectOption>> dependents(
            FormConfigStore store, String formid,
            String propertyName, String propertyValue, Locale locale) {
        
        final FormConfigDTO formConfig = store.getOrException(formid);

        final Object modelobject = formConfig.getModelobject();

        final Map<String, List<SelectOption>> result = this.dependentsProvider
                .getChoicesForDependents(modelobject, propertyName, propertyValue, locale);

        log.debug("{}#{} {} = {}", formConfig.getModelname(), 
                propertyName, FormStages.dependents, result);

        return result;
    }
    
    public FormConfigDTO validateSingle(FormConfigStore store, String formid,
            String propertyName, String propertyValue) {
            
        final FormConfigDTO formConfig = store.getOrException(formid);
        
        final Object modelobject = formConfig.getModelobject();
        
        final BindingResult bindingResult = bindingValidator
                .bindAndValidateSingle(modelobject, propertyName, propertyValue)
                .getBindingResult();
        
        this.validateAndAddErrors(formConfig, bindingResult, propertyName);

        log.debug("{}#{} {} = {}", formConfig.getModelname(), 
                propertyName, FormStages.validateSingle, 
                bindingResult.hasErrors() ? "errors" : "no errors");
        
        return formConfig;
    }

    private void validateAndAddErrors(FormConfigDTO formConfig, BindingResult bindingResult) {
        this.validateAndAddErrors(formConfig, bindingResult, null);
    }
    
    private void validateAndAddErrors(FormConfigDTO formConfig, BindingResult bindingResult, String propertyName) {
    
        formConfig.setBindingResult(bindingResult);
        
        this.formValidatorService.validateModelObject(formConfig);

        log.debug("Done validation, has errors: {}", bindingResult.hasErrors());
        log.trace("All errors: {}", bindingResult.getAllErrors());

        if(StringUtils.isNullOrEmpty(propertyName)) {
            if(bindingResult.hasErrors()) {
                formConfig.setErrors(bindingErrorCollector.getErrors(bindingResult));
            }else{
                formConfig.setErrors(null);
            }
        }else{
            if(bindingResult.hasFieldErrors(propertyName)) {
                formConfig.setErrors(bindingErrorCollector.getFieldErrors(bindingResult, propertyName));
            }else{
                formConfig.removeAllErrors(propertyName);
            }
        }
    }
    
    private void check(FormConfig formConfig){
        Objects.requireNonNull(formConfig.getCrudAction());
        Objects.requireNonNull(formConfig.getFormid());
        Objects.requireNonNull(formConfig.getModelname());
        Objects.requireNonNull(formConfig.getModelobject());
    }

    public DependentsProvider getDependentsProvider() {
        return dependentsProvider;
    }

    public FormValidatorService getFormValidatorService() {
        return formValidatorService;
    }

    public Optional<FileUploadHandler> getFileUploadService() {
        return Optional.ofNullable(fileUploadHandlerOptional);
    }

    public FormSubmitHandler getFormSubmitHandler() {
        return formSubmitHandler;
    }

    public ModelObjectService getModelObjectService() {
        return modelObjectService;
    }
}
