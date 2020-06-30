package com.looseboxes.webform.services;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.form.DependentsProvider;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigBean;
import com.looseboxes.webform.form.FormSubmitHandler;
import com.looseboxes.webform.web.FormRequest;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

/**
 * @author hp
 */
@Service
public class FormService<T> {
    
    private final Logger log = LoggerFactory.getLogger(FormService.class);
    
    @Autowired private ModelObjectService modelObjectService;
    @Autowired private TypeFromNameResolver typeFromNameResolver;
    @Autowired private MessageAttributesService messageAttributesService;
    @Autowired private DependentsProvider dependentsProvider;
    @Autowired private FormValidatorService formValidatorService;
    @Autowired private FileUploadService fileUploadService;
    @Autowired private FormSubmitHandler formSubmitHandler;
    
    public FormRequest onBeginForm(FormRequest formRequest){
        
        formRequest = modelObjectService.onBeginForm(formRequest);
        
        log.debug("{}", formRequest);
        
        //////////////////////////// IMPORTANT NOTE ////////////////////////////
        // When we didn't clear this model, the session was getting re-populated
        // by the modelobject even after the modelobject had been removed from
        // the session.
        // Two attributes were identified in the ModelMap: formConfigBean and 
        // org.springframework.validation.BindingResult.formConfigBean. 
        ////////////////////////////////////////////////////////////////////////
        // 
        final ModelMap model = formRequest.getModelMap();
        final FormConfigBean formConfig = formRequest.getFormConfig();
        model.clear();
        model.putAll(formConfig.toMap());
        
        formRequest.getAttributeService().setSessionAttribute(formConfig);
        formRequest.getAttributeService().sessionAttributes().put(
                HttpSessionAttributes.MODELOBJECT, formConfig.getModelobject());
        
        return formRequest;
    }
    
    public FormRequest onValidateForm(
            T modelobject,
            BindingResult bindingResult,
            FormRequest formRequest) {
        
        
        formRequest = modelObjectService.onValidateForm(formRequest, modelobject);
        
        log.debug("{}", formRequest);
        
        final FormConfigBean formConfig = formRequest.getFormConfig();

        this.formValidatorService.validateModelObject(
                bindingResult, formRequest.getModelMap(), formConfig);
        
        this.check(formConfig);
  
        final Map<String, Object> formParams = formConfig.toMap();
        
        final FormAttributeService formAttributeService = formRequest.getAttributeService();

        formAttributeService.modelAttributes().putAll(formParams);
        
        if ( ! bindingResult.hasErrors() && formRequest.hasFiles()) {
            
            final Collection<String> uploadedFiles = 
                    fileUploadService.upload(formRequest);

            formAttributeService.addUploadedFiles(uploadedFiles);
        }

        return formRequest;
    }    

    public FormRequest onSubmitForm(FormRequest formRequest) {
        
        formRequest = modelObjectService.onSubmitForm(formRequest);
        
        log.debug("{}", formRequest);
        
        final FormConfigBean formConfig = formRequest.getFormConfig();

        this.check(formConfig);

        final FormAttributeService formAttributeService = 
                formRequest.getAttributeService();
        
        try{
        
            this.formSubmitHandler.process(formConfig);
            
            formAttributeService.removeUploadedFiles(null);
            
            //@Todo periodic job to check uploads dir for orphan files and deleteManagedEntity
            this.addSuccessMessagesToModel(formRequest.getModelMap(), formConfig);

            if(formConfig.getForm().getParent() != null &&
                    formConfig.getModelobject() != null) {

                modelObjectService.updateParentForm(formRequest);
            }
            
        }catch(RuntimeException e) {

            formAttributeService.deleteUploadedFiles();
            
            this.addErrorMessagesToModel(formRequest.getModelMap(), formConfig, e);
            
            throw e;
            
        }finally{
            
            formAttributeService.removeSessionAttribute(formConfig.getFormid());
//            formAttributeService.sessionAttributes().remove(HttpSessionAttributes.MODELOBJECT);
            formAttributeService.remove(HttpSessionAttributes.MODELOBJECT);
            formAttributeService.removeAll(FormConfig.names());
        }
        
        return formRequest;
    } 

    public Map<String, Map> dependents(
            ModelMap model, FormConfig formConfig,
            String propertyName, String propertyValue, Locale locale) {

        final Object modelobject = formConfig.getModelobject();

        final Map<String, Map> result = this.dependentsProvider
                .getChoicesForDependents(modelobject, propertyName, propertyValue, locale);

        log.debug("{}#{} {} = {}", formConfig.getModelname(), 
                propertyName, FormStage.dependents, result);

        return result;
    }
    
    public FormConfig validateSingle(
            T modelobject, BindingResult bindingResult, ModelMap model, 
            FormConfig formConfig, String propertyName, String propertyValue) {
            
//            final Object modelobject = formConfig.getModelobject();

//            final BindingResult bindingResult = 
//                    this.validateSingle(modelobject, propertyName, propertyValue);

        if(bindingResult.hasFieldErrors(propertyName)) {

            this.messageAttributesService
                    .addErrorToModel(bindingResult, model, propertyName);
        }

        this.formValidatorService.validateModelObject(
                bindingResult, model, formConfig, modelobject);

        return formConfig;
    }
    
    private void check(FormConfig formConfig){

        Objects.requireNonNull(formConfig.getCrudAction());
        Objects.requireNonNull(formConfig.getFormid());
        final String modelname = Objects.requireNonNull(formConfig.getModelname());
        final Object modelobject = Objects.requireNonNull(formConfig.getModelobject());
        
        final String foundname = this.typeFromNameResolver.getName(modelobject.getClass());
        
        if( ! modelname.equalsIgnoreCase(foundname)) {
            
            log.warn("Expected name: {}, found name: {} from type: {}", 
                    modelname, foundname, modelobject.getClass());
        
            throw Errors.unexpectedModelName(modelname, foundname);
        }
    }

    public void addSuccessMessagesToModel(ModelMap model, FormConfig formConfig) {

        log.debug("SUCCESS: {}", formConfig);
            
        final Object m = "Successfully completed action: " + 
                formConfig.getCrudAction() + ' ' + formConfig.getModelname();
        
        this.messageAttributesService.addInfoMessage(model, m);
    }
    
    public void addErrorMessagesToModel(
            ModelMap model, FormConfig formConfig, Exception exception) {
    
        log.warn("Failed to process: " + formConfig, exception);

        this.messageAttributesService.addErrorMessages(model, 
                "Unexpected error occured while processing action: " + 
                        formConfig.getCrudAction() + ' ' + formConfig.getModelname());
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

    public MessageAttributesService getMessageAttributesService() {
        return messageAttributesService;
    }

    public ModelObjectService getModelObjectService() {
        return modelObjectService;
    }
}
