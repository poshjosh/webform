package com.looseboxes.webform;

import com.looseboxes.webform.form.FormRequestParams;
import com.looseboxes.webform.services.FileUploadService;
import com.looseboxes.webform.services.MessageAttributesService;
import com.looseboxes.webform.services.FormService;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.services.AttributeService;
import com.looseboxes.webform.store.StoreDelegate;
import java.io.FileNotFoundException;
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

/**
 * @author hp
 */
//@see https://stackoverflow.com/questions/30616051/how-to-post-generic-objects-to-a-spring-controller
@SessionAttributes({ModelAttributes.MODELOBJECT}) 
public class FormController implements CrudActionNames{
    
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
    
    @RequestMapping("/") 
    public String home(){
        // @TODO stream README.md
        return Templates.HOME;
    }

    @GetMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public String showForm(ModelMap model, 
            @PathVariable(name=Params.ACTION, required=true) String action,
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            HttpServletRequest request, HttpServletResponse response) 
            throws FileNotFoundException{
        
        final FormService formSvc = getFormService(model, request);
        
        final Object modelobject = formSvc.begin(action, modelname, modelid);
        
        final FormRequestParams formReqParams = formSvc
                .toRequestParams(action, modelname, modelid, modelobject, modelfields);
        
        if(LOG.isTraceEnabled()) {
            this.trace("showForm", model, formReqParams, request, response);
        }

        LOG.debug("{}", formReqParams);
        
        final AttributeService attributeSvc = this.getAttributeService(model, request);

        attributeSvc.modelAttributes().putAll(formReqParams.toMap());
        
        return formSvc.getTemplateForShowingForm(action);
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
            HttpServletRequest request, HttpServletResponse response) {
        
        final FormService formSvc = getFormService(model, request);
        
        final FormRequestParams formReqParams = formSvc.toRequestParams(
                action, formid, modelname, modelid, modelobject, modelfields);

        if(LOG.isTraceEnabled()) {
            this.trace("validateForm", model, formReqParams, request, response);
        }
        
        LOG.debug("{}", formReqParams);
        
        final List<Validator> validators = this.formValidatorFactory.get(formReqParams);

        for(Validator validator : validators) {

            ValidationUtils.invokeValidator(validator, modelobject, bindingResult);
        }
        
        final Map<String, Object> formParams = formReqParams.toMap();
        
        formSvc.checkAll(formReqParams);
  
        final String target;
        
        if (bindingResult.hasErrors()) {
            
            this.messageAttributesSvc.addErrorsToModel(bindingResult, model);
            
            target = Templates.FORM;
            
        }else{
            
            final AttributeService attributeSvc = getAttributeService(model, request);
            
            if(request instanceof MultipartHttpServletRequest) {
            
                final Collection<String> uploadedFiles = fileUploadSvc.upload(
                        modelname, modelobject, (MultipartHttpServletRequest)request);

                attributeSvc.addUploadedFiles(uploadedFiles);
            }
            
            attributeSvc.putAll(formParams);
            
            attributeSvc.sessionAttributes()
                    .put(getAttributeName(formReqParams.getFormid()), formReqParams);
            
            target = Templates.FORM_CONFIRMATION;
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
            HttpServletRequest request, HttpServletResponse response) {
        
        final FormService formSvc = getFormService(model, request);

        final AttributeService attributeSvc = getAttributeService(model, request);
        
        final String formAttributeName = this.getAttributeName(formid);

        final FormRequestParams formReqParams = (FormRequestParams)attributeSvc
                .sessionAttributes().getOrDefault(formAttributeName, null);
        
        if(LOG.isTraceEnabled()) {
            this.trace("submitForm", model, formReqParams, request, response);
        }
        
        LOG.debug("{}", formReqParams);

        formSvc.checkAll(formReqParams);

        String target;

        try{
        
            this.onFormSubmitted.onFormSubmitted(formReqParams);
            
            attributeSvc.removeUploadedFiles(null);
            
            //@Todo periodic job to check uploads dir for orphan files and deleteManagedEntity

            target = this.onFormSubmitSuccessful(model, formReqParams, request, response);
            
        }catch(RuntimeException e) {

            attributeSvc.deleteUploadedFiles();
            
            target = this.onFormSubmitFailed(model, formReqParams, e, request, response);
            
        }finally{
            
            attributeSvc.removeAll(new String[]{
                formAttributeName, ModelAttributes.MODELOBJECT});
        }
        
        LOG.debug("Target: {}", target);
        
        return target;
    } 
    
    public String getAttributeName(String formid) {
        return com.looseboxes.webform.SessionAttributes.forFormId(formid);    
    }

    public String onFormSubmitSuccessful(
            ModelMap model, FormRequestParams formReqParams,
            HttpServletRequest request, HttpServletResponse response) {

        LOG.debug("SUCCESS: {}", formReqParams);
            
        final Object m = "Successfully completed action: " + 
                formReqParams.getAction() + ' ' + formReqParams.getModelname();
        
        this.messageAttributesSvc.addInfoMessage(model, m);
        
        request.setAttribute(ModelAttributes.MESSAGES, Collections.singletonList(m));
        
        return getTargetAfterSubmit(formReqParams).orElse(Templates.SUCCESS);
    }
    
    public Optional<String> getTargetAfterSubmit(FormRequestParams formReqParams) {
        return Optional.empty();
    }    
    
    public String onFormSubmitFailed(
            ModelMap model, FormRequestParams formReqParams, Exception e,
            HttpServletRequest request, HttpServletResponse response) {
    
        LOG.warn("Failed to process: " + formReqParams, e);

        this.messageAttributesSvc.addErrorMessages(model, 
                "Unexpected error occured while processing action: " + 
                        formReqParams.getAction() + ' ' + formReqParams.getModelname());

        return Templates.ERROR;
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

        final ModelAndView modelAndView = new ModelAndView(Templates.ERROR);

        // 1MB is the default, if none is set in properties file
        final String max = environment.getProperty(
                SpringProperties.MULTIPART_MAX_FILE_SIZE, "1MB");
        
        final String error = 
                "One or more files, you tried to upload exceeds the max of " + max;
        
        messageAttributesSvc.addErrorMessage(modelAndView.getModel(), error);
        
        return modelAndView;
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ModelAndView handleFileNotFound(
        FileNotFoundException exception, 
        HttpServletRequest request,  
        HttpServletResponse response) {
  
        final ModelAndView modelAndView = new ModelAndView(Templates.ERROR);
        
        messageAttributesSvc.addErrorMessage(modelAndView.getModel(), 
                "The file you requested was not found at: " + 
                        "<br/><small>" + request.getRequestURI() + "</small>" + 
                        "<p>It may have been moved, or it is no longer available</p>" +
                        "Also, check that you entered the correct address in the browser." +
                        "<p>However, keep calm, keep browsing</p>");
        
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        
        return modelAndView;
    }
    
    private void trace(String method, Object modelMap, FormRequestParams params,
            HttpServletRequest request, HttpServletResponse response) {
        if(LOG.isTraceEnabled()) {
            LOG.trace("==================== " + method + " ====================");
            final Print print = new Print();
            params.toMap().forEach((k, v) -> {
                print.add(k, v);
            });
            print.add("ModelMap", modelMap)
            .addHttpRequest(request)
            .addHttpSession(request.getSession())
            .traceAdded();
        }
    }
}
/**
 * 
    
    // When we re-directed to say the form page, the entire gamut of attributes
    // required for that page to work was missing. Errors everwhere
    //
    private String getTarget(HttpServletRequest request, String resultIfNone){
        final String uri = request.getRequestURI();
        String target = resultIfNone;
        if(uri != null && ! uri.isEmpty()) {
            if(uri.contains("/validate")) {
                target = Templates.FORM;
            }else if(uri.contains("/submit")) {
                target = Templates.FORM_CONFIRMATION;
            }else{
                target = resultIfNone;
            }
        }
        LOG.debug("Request.URI: {}, target: {}", uri, target);
        return target;
    }
 * 
 */