package com.looseboxes.webform.services;

import com.bc.webform.choices.SelectOption;
import com.looseboxes.webform.web.BindingResultErrorCollector;
import com.looseboxes.webform.form.DependentsProvider;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.form.FormSubmitHandler;
import com.looseboxes.webform.web.FormRequest;
import com.looseboxes.webform.web.WebValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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
import com.looseboxes.webform.util.StringUtils;
import java.util.List;

/**
 * @author hp
 */
@Service
public class FormService<T> {
    
    private final Logger log = LoggerFactory.getLogger(FormService.class);
    
    @Autowired private ModelObjectService modelObjectService;
    @Autowired private BindingResultErrorCollector bindingErrorCollector;
    @Autowired private DependentsProvider dependentsProvider;
    @Autowired private WebValidator webValidator;
    @Autowired private FormValidatorService formValidatorService;
    @Autowired private FileUploadService fileUploadService;
    @Autowired private FormSubmitHandler formSubmitHandler;
    
    public FormRequest onBeginForm(FormRequest formRequest){
        
        formRequest = modelObjectService.onBeginForm(formRequest);
        
        log.trace("{}", formRequest);
        
        final FormConfigDTO formConfig = formRequest.getFormConfig();
        formRequest.getAttributeService().setSessionAttribute(formConfig);
        
        return formRequest;
    }

    public FormRequest onValidateForm(FormRequest formRequest, WebRequest webRequest) {
        
        formRequest = modelObjectService.onValidateForm(formRequest, null);
        
        log.trace("{}", formRequest);
        
        final FormConfigDTO formConfig = formRequest.getFormConfig();

        final BindingResult bindingResult = webValidator
                .bindAndValidate(webRequest, formConfig.getModelobject())
                .getBindingResult();
        
        this.validateAndAddErrors(formConfig, bindingResult);
        
        this.check(formConfig);
  
        if ( ! bindingResult.hasErrors() && formRequest.hasFiles()) {
            
            final Collection<String> uploadedFiles = 
                    fileUploadService.upload(formRequest);

            formConfig.setUploadedFiles(uploadedFiles);
        }

        return formRequest;
    }    

    public FormRequest onSubmitForm(FormRequest formRequest) {
        
        formRequest = modelObjectService.onSubmitForm(formRequest);
        
        log.trace("{}", formRequest);
        
        final FormConfigDTO formConfig = formRequest.getFormConfig();

        this.check(formConfig);

        final FormAttributeService formAttributeService = 
                formRequest.getAttributeService();
        
        try{
        
            this.formSubmitHandler.process(formRequest);

            if(formConfig.getForm().getParent() != null &&
                    formConfig.getModelobject() != null) {

                modelObjectService.updateParentForm(formRequest);
            }
            
        }catch(RuntimeException e) {

            this.deleteUploadedFiles(formConfig);
            
            throw e;
            
        }finally{
            
            formAttributeService.removeSessionAttribute(formConfig.getFormid());
            formAttributeService.removeAll(FormConfig.names());
        }
        
        return formRequest;
    } 

    public Map<String, List<SelectOption>> dependents(FormConfig formConfig,
            String propertyName, String propertyValue, Locale locale) {

        final Object modelobject = formConfig.getModelobject();

        final Map<String, List<SelectOption>> result = this.dependentsProvider
                .getChoicesForDependents(modelobject, propertyName, propertyValue, locale);

        log.debug("{}#{} {} = {}", formConfig.getModelname(), 
                propertyName, FormStages.dependents, result);

        return result;
    }
    
    public void validateSingle(FormConfigDTO formConfig, 
            String propertyName, String propertyValue) {
            
        final Object modelobject = formConfig.getModelobject();
        
        final BindingResult bindingResult = webValidator
                .bindAndValidateSingle(modelobject, propertyName, propertyValue)
                .getBindingResult();
        
        this.validateAndAddErrors(formConfig, bindingResult, propertyName);

        log.debug("{}#{} {} = {}", formConfig.getModelname(), 
                propertyName, FormStages.validateSingle, 
                bindingResult.hasErrors() ? "errors" : "no errors");
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
                formConfig.addErrors(bindingErrorCollector.getErrors(bindingResult));
            }else{
                formConfig.setErrors(null);
            }
        }else{
            if(bindingResult.hasFieldErrors(propertyName)) {
                formConfig.addErrors(bindingErrorCollector.getFieldErrors(bindingResult, propertyName));
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

    public void deleteUploadedFiles(FormConfigDTO formConfig) {
        final Collection<String> files = formConfig.removeUploadedFiles();
        if(files == null || files.isEmpty()) {
            return;
        }
        for(String file : files) {
            final Path path = Paths.get(file).toAbsolutePath().normalize();
            // @TODO
            // Walk through files to local disc and delete orphans (i.e those
            // without corresponding database entry), aged more than a certain
            // limit, say 24 hours.s
            try{
                if( ! Files.deleteIfExists(path)) {
                    log.info("Will delete on exit: {}", path);
                    path.toFile().deleteOnExit();
                }
            }catch(IOException e) {
                log.warn("Problem deleting: " + path, e);
                log.info("Will delete on exit: {}", path);
                path.toFile().deleteOnExit();
            }
        }
    }

    public DependentsProvider getDependentsProvider() {
        return dependentsProvider;
    }

    public FormValidatorService getFormValidatorService() {
        return formValidatorService;
    }

    public FileUploadService getFileUploadService() {
        return fileUploadService;
    }

    public FormSubmitHandler getFormSubmitHandler() {
        return formSubmitHandler;
    }

    public ModelObjectService getModelObjectService() {
        return modelObjectService;
    }
}
/**
 * 
    
    private void fixModelObjectBug(FormRequest formRequest) {
        //////////////////////////// IMPORTANT NOTE ////////////////////////////
        // When we didn't clear this model, the session was getting re-populated
        // by the modelobject even after the modelobject had been removed from
        // the session.
        // Two attributes were identified in the ModelMap: formConfigBean and 
        // org.springframework.validation.BindingResult.formConfigBean 
        ////////////////////////////////////////////////////////////////////////
        // 
        final AttributeStore<ModelMap> modelStore = formRequest.getAttributeService().modelAttributes();
        modelStore.removeAll(FormConfig.names());
        final String name = "formConfigBean";
        modelStore.removeAll(new String[]{name, org.springframework.validation.BindingResult.class.getName()+"."+name});
    }
    
    private void fixThymeleafIdAttributeBug(FormRequest formRequest) {
        
        final AttributeStore<ModelMap> modelStore = formRequest.getAttributeService().modelAttributes();
        
        // In Thymeleaf template we could not reference a variable named 'id'
        // thus ${id}. 'Params.MODEL_ID' - refers to a variable named 'id', so
        // we add an alias here i.e 'modelid'
        //
        modelStore.put("modelid", formRequest.getFormConfig().getId());
    }
 * 
 */