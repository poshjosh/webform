package com.looseboxes.webform.services;

import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.FormConfigBean;
import com.looseboxes.webform.form.FormSubmitHandler;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

/**
 * @author hp
 */
@Service
public class FormControllerService<T> {
    
    private final Logger log = LoggerFactory.getLogger(FormControllerService.class);
    
    @Autowired private MessageAttributesService messageAttributesService;
    @Autowired private FormValidatorService formValidatorService;
    @Autowired private FileUploadService fileUploadService;
    @Autowired private FormSubmitHandler formSubmitHandler;

    public FormConfigBean onBeginForm(
            ModelMap model, FormConfigBean formConfig, final FormService formSvc){
        
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
        
        formSvc.getFormAttributeService().setSessionAttribute(formConfig);
        formSvc.getFormAttributeService().sessionAttributes().put(
                HttpSessionAttributes.MODELOBJECT, formConfig.getModelobject());
        
        return formConfig;
    }
    
    public FormConfigBean onValidateForm(
            T modelobject,
            BindingResult bindingResult,
            ModelMap model,
            FormConfigBean formConfig,
            FormService formSvc,
            @Nullable FileUploadConfig fileUploadConfig) {
        
        formConfig = formSvc.onValidateForm(formConfig, modelobject);

        log.debug("{}", formConfig);
        
        this.formValidatorService.validateModelObject(bindingResult, model, formConfig);
        
        formSvc.checkAll(formConfig);
  
        final Map<String, Object> formParams = formConfig.toMap();
        
        final AttributeService attributeSvc = formSvc.getAttributeService();

        attributeSvc.modelAttributes().putAll(formParams);
        
        if ( ! bindingResult.hasErrors()) {
            
            if(fileUploadConfig != null && fileUploadConfig.hasFiles()) {
                
                final Collection<String> uploadedFiles = 
                        fileUploadService.upload(fileUploadConfig);

                attributeSvc.addUploadedFiles(uploadedFiles);
            }
        }

        return formConfig;
    }    

    public FormConfigBean onSubmitForm(
            ModelMap model, FormConfigBean formConfig, FormService formSvc) {
        
        formConfig = formSvc.onSubmitForm(formConfig);
        
        log.debug("{}", formConfig);

        formSvc.checkAll(formConfig);

        final AttributeService attributeSvc = formSvc.getAttributeService();
        
        try{
        
            this.formSubmitHandler.process(formConfig);
            
            attributeSvc.removeUploadedFiles(null);
            
            //@Todo periodic job to check uploads dir for orphan files and deleteManagedEntity

            this.addSuccessMessagesToModel(model, formConfig);

        }catch(RuntimeException e) {

            attributeSvc.deleteUploadedFiles();
            
            this.addErrorMessagesToModel(model, formConfig, e);
            
            throw e;
            
        }finally{
            
            formSvc.getFormAttributeService().removeSessionAttribute(formConfig.getFormid());
//            attributeSvc.sessionAttributes().remove(HttpSessionAttributes.MODELOBJECT);
            attributeSvc.remove(HttpSessionAttributes.MODELOBJECT);
            attributeSvc.removeAll(FormConfig.names());
        }
        
        return formConfig;
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
}
