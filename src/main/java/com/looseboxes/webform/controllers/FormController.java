package com.looseboxes.webform.controllers;

import com.looseboxes.webform.FormEndpoints;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.ModelAttributes;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.util.Print;
import com.looseboxes.webform.SpringProperties;
import com.looseboxes.webform.exceptions.RouteException;
import com.looseboxes.webform.exceptions.TargetNotFoundException;
import com.looseboxes.webform.form.FormRequestParams;
import com.looseboxes.webform.services.FileUploadService;
import com.looseboxes.webform.services.MessageAttributesService;
import com.looseboxes.webform.services.FormService;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.services.AttributeService;
import com.looseboxes.webform.store.StoreDelegate;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.looseboxes.webform.CrudAction;

/**
 * @author hp
 */
//@see https://stackoverflow.com/questions/30616051/how-to-post-generic-objects-to-a-spring-controller
@SessionAttributes({ModelAttributes.MODELOBJECT}) 
public class FormController{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormController.class);
    
    @FunctionalInterface
    public static interface OnFormSubmitted{
        void onFormSubmitted(FormRequestParams formReqParams);
    }
    
    @Autowired private Environment environment;
    @Autowired private FormService genericFormSvc;
    @Autowired private FormValidatorFactory formValidatorFactory;
    @Autowired private AttributeService genericAttributeSvc;
    @Autowired private MessageAttributesService messageAttributesSvc;
    @Autowired private FileUploadService fileUploadSvc;
    @Autowired private OnFormSubmitted onFormSubmitted;

    public FormController() { }

    @GetMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public String showForm(ModelMap model, 
            @PathVariable(name=Params.ACTION, required=true) String action,
            @RequestParam(value=Params.FORMID, required=false) String formid,
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            @RequestParam(value=Params.PARENT_FORMID, required=false) String parentFormId,
            @RequestParam(value=Params.TARGET_ON_COMPLETION, required=false) String targetOnCompletion,
            HttpServletRequest request, HttpServletResponse response) 
            throws FileNotFoundException{
        
        final CrudAction crudAction = CrudAction.valueOf(action);
        
        final FormService formSvc = getFormService(model, request);
        
        final Object modelobject = formSvc.begin(crudAction, modelname, modelid);
        
        final FormRequestParams formReqParams = formSvc
                .params(crudAction, modelname, modelid, modelobject, 
                        modelfields, parentFormId, 
                        getTargetOnCompletion(parentFormId, targetOnCompletion));
        
        if(LOG.isTraceEnabled()) {
            this.trace("showForm", model, formReqParams, request, response);
        }

        LOG.debug("{}", formReqParams);
        
        final AttributeService attributeSvc = this.getAttributeService(model, request);
        
        attributeSvc.modelAttributes().putAll(formReqParams.toMap());
        
        formSvc.setSessionAttribute(formReqParams);
        
        return formSvc.getFormEndpoints().forCrudAction(crudAction);
    }
    
    public String getTargetOnCompletion(String parentFormId, String val) {
        if(val == null || val.isEmpty()) {
            return null;
        }else if(parentFormId == null|| parentFormId.isEmpty()){
            return val;
        }else {
            final String toAdd = Params.FORMID + '=' + parentFormId;
            if( ! val.contains(toAdd)) {
                final String joiner = val.contains("?") ? "&" : "?";
                return val + joiner + toAdd;
            }
            return val;
        }
    }

    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/validate")
    public String validateForm(
            @Valid @ModelAttribute(ModelAttributes.MODELOBJECT) Object modelobject,
            BindingResult bindingResult,
            ModelMap model,
            @PathVariable(name=Params.ACTION, required=true) String action,
            @RequestParam(value=Params.FORMID, required=true) String formid,
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            @RequestParam(value=Params.PARENT_FORMID, required=false) String parentFormId,
            @RequestParam(value=Params.TARGET_ON_COMPLETION, required=false) String targetOnCompletion,
            HttpServletRequest request, HttpServletResponse response) {
        
        final CrudAction crudAction = CrudAction.valueOf(action);

        final FormService formSvc = getFormService(model, request);

        final FormRequestParams formReqParams = formSvc
                .paramsForValidate(crudAction, formid, modelname, modelid, 
                modelobject, modelfields, parentFormId, targetOnCompletion);

        if(LOG.isTraceEnabled()) {
            this.trace("validateForm", model, formReqParams, request, response);
        }
        
        LOG.debug("{}", formReqParams);
        
        final List<Validator> validators = this.formValidatorFactory.get(formReqParams);

        for(Validator validator : validators) {

            ValidationUtils.invokeValidator(validator, modelobject, bindingResult);
        }
        
        formSvc.checkAll(formReqParams);
  
        final Map<String, Object> formParams = formReqParams.toMap();
        
        final AttributeService attributeSvc = getAttributeService(model, request);

        attributeSvc.modelAttributes().putAll(formParams);
        
        final String target;
        
        final FormEndpoints endpoints = this.genericFormSvc.getFormEndpoints();
        
        if (bindingResult.hasErrors()) {
            
            this.messageAttributesSvc.addErrorsToModel(bindingResult, model);
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            target = endpoints.getForm();
            
        }else{
            
            if(request instanceof MultipartHttpServletRequest) {
            
                final Collection<String> uploadedFiles = fileUploadSvc.upload(
                        modelname, modelobject, (MultipartHttpServletRequest)request);

                attributeSvc.addUploadedFiles(uploadedFiles);
            }
            
            target = endpoints.getFormConfirmation();
        }

        return target;
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/submit")
    public String submitForm(
            ModelMap model,
            @PathVariable(name=Params.ACTION, required=true) String action,
            @RequestParam(value=Params.FORMID, required=true) String formid,
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            @RequestParam(value=Params.PARENT_FORMID, required=false) String parentFormId,
            @RequestParam(value=Params.TARGET_ON_COMPLETION, required=false) String targetOnCompletion,
            HttpServletRequest request, HttpServletResponse response) {
        
        final CrudAction crudAction = CrudAction.valueOf(action);
        
        final FormService formSvc = getFormService(model, request);
        
        final FormRequestParams formReqParams = formSvc.paramsForSubmit(
                crudAction, formid, modelname, modelid, 
                modelfields, parentFormId, targetOnCompletion);
        
        if(LOG.isTraceEnabled()) {
            this.trace("submitForm", model, formReqParams, request, response);
        }
        
        LOG.debug("{}", formReqParams);

        formSvc.checkAll(formReqParams);

        String target;

        final AttributeService attributeSvc = getAttributeService(model, request);
        
        try{
        
            this.onFormSubmitted.onFormSubmitted(formReqParams);
            
            attributeSvc.removeUploadedFiles(null);
            
            //@Todo periodic job to check uploads dir for orphan files and deleteManagedEntity

            target = this.onFormSubmitSuccessful(model, formReqParams, request, response);
            
            if(CrudAction.create.equals(crudAction)) {
                try{
                    formSvc.updateParentWithNewlyCreated(formReqParams);
                }catch(RuntimeException e) {
                    LOG.warn("Failed to update parent with this form's value", e);
                }
            }
        }catch(RuntimeException e) {

            attributeSvc.deleteUploadedFiles();
            
            target = this.onFormSubmitFailed(model, formReqParams, e, request, response);
            
        }finally{
            
            formSvc.removeSessionAttribute(formReqParams.getFormid());
            attributeSvc.sessionAttributes().remove(HttpSessionAttributes.MODELOBJECT);
        }
        
        LOG.debug("Target: {}", target);
        
        return target;
    } 

    public String onFormSubmitSuccessful(
            ModelMap model, FormRequestParams formReqParams,
            HttpServletRequest request, HttpServletResponse response) {

        LOG.debug("SUCCESS: {}", formReqParams);
            
        final Object m = "Successfully completed action: " + 
                formReqParams.getAction() + ' ' + formReqParams.getModelname();
        
        this.messageAttributesSvc.addInfoMessage(model, m);
        
        request.setAttribute(ModelAttributes.MESSAGES, Collections.singletonList(m));
        
        return getTargetAfterSubmit(formReqParams).orElse(getSuccessEndpoint());
    }
    
    public Optional<String> getTargetAfterSubmit(FormRequestParams formReqParams) {
        final String targetOnCompletion = formReqParams.getTargetOnCompletion();
        return targetOnCompletion == null ? Optional.empty() : 
                Optional.of("redirect:" + targetOnCompletion);
    }    
    
    public String onFormSubmitFailed(
            ModelMap model, FormRequestParams formReqParams, Exception e,
            HttpServletRequest request, HttpServletResponse response) {
    
        LOG.warn("Failed to process: " + formReqParams, e);

        this.messageAttributesSvc.addErrorMessages(model, 
                "Unexpected error occured while processing action: " + 
                        formReqParams.getAction() + ' ' + formReqParams.getModelname());

        return this.getErrorEndpoint();
    }
    
    private AttributeService getAttributeService(
            ModelMap model, HttpServletRequest request) {
    
        final StoreDelegate delegate = new StoreDelegate(model, request);
        
        return this.genericAttributeSvc.wrap(delegate);
    }
    
    private FormService getFormService(ModelMap model, HttpServletRequest request) {
        
        final StoreDelegate delegate = new StoreDelegate(model, request);
        
        final FormService formSvc = this.genericFormSvc.wrap(delegate);
        
        return formSvc;
    }    
    
    //////////////////////////////////////////////////////////////////////////
    // Exception handling
    // @see https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
    //////////////////////////////////////////////////////////////////////////

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSizeExceeded(
        MaxUploadSizeExceededException exception, 
        HttpServletRequest request,  
        HttpServletResponse response) {

// when form value entered, referer format:  http://.../create/blog     
// RequestURI for above referer:             http://.../create/blog/validate
//        final String referer = request.getHeader("referer");

        final String endpoint = this.getErrorEndpoint();

        final ModelAndView modelAndView = new ModelAndView(endpoint);

        // 1MB is the default, if none is set in properties file
        final String max = environment.getProperty(
                SpringProperties.MULTIPART_MAX_FILE_SIZE, "1MB");
        
        final String error = 
                "One or more files, you tried to upload exceeds the max of " + max;
        
        messageAttributesSvc.addErrorMessage(modelAndView.getModel(), error);
        
        LOG.warn(error, exception);
        
        return modelAndView;
    }

    @ExceptionHandler(TargetNotFoundException.class)
    public ModelAndView handleFileNotFound(
        TargetNotFoundException exception, 
        HttpServletRequest request,  
        HttpServletResponse response) {
  
        final String endpoint = this.getErrorEndpoint();
        
        final ModelAndView modelAndView = new ModelAndView(endpoint);
  
        final List<String> errors = Arrays.asList(
                "The page you request was not found",
                "It may have been moved, or it is no longer available",
                "Also, check that you entered the correct address in the browser.",
                "",
                request.getRequestURI(),
                "",
                "Meanwhile, keep calm, keep browsing");
        
        messageAttributesSvc.addErrorMessage(modelAndView.getModel(), errors);

        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        
        LOG.warn(errors.toString(), exception);

        return modelAndView;
    }

    @ExceptionHandler(RouteException.class)
    public ModelAndView handleFileNotFound(
        RouteException exception, 
        HttpServletRequest request,  
        HttpServletResponse response) {
  
        final String endpoint = this.getErrorEndpoint();
        
        final ModelAndView modelAndView = new ModelAndView(endpoint);
  
        final List<String> errors = Arrays.asList(
                "We are unable to fullfill your request at this time.",
                "",
                request.getRequestURI(),
                "",
                "Meanwhile, keep calm, keep browsing");
        
        messageAttributesSvc.addErrorMessage(modelAndView.getModel(), errors);

        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        
        LOG.warn(errors.toString(), exception);

        return modelAndView;
    }
    
    private String getErrorEndpoint() {
        final String endpoint = this.genericFormSvc.getFormEndpoints().getError();
        return endpoint;
    }
    
    private String getSuccessEndpoint() {
        final String endpoint = this.genericFormSvc.getFormEndpoints().getSuccess();
        return endpoint;
    }
    
    private void trace(String method, Object modelMap, FormRequestParams params,
            HttpServletRequest request, HttpServletResponse response) {
        if(LOG.isTraceEnabled()) {
            LOG.trace("==================== " + method + " ====================");
            final Print print = new Print();
            if(params != null) {
                params.toMap().forEach((k, v) -> {
                    print.add(k, v);
                });
            }
            print.add("ModelMap", modelMap)
            .addHttpRequest(request)
            .addHttpSession(request.getSession())
            .traceAdded();
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public FormService getGenericFormSvc() {
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
