package com.looseboxes.webform.controllers;

import com.looseboxes.webform.FormEndpoints;
import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.SpringProperties;
import com.looseboxes.webform.exceptions.RouteException;
import com.looseboxes.webform.exceptions.ResourceNotFoundException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import com.looseboxes.webform.web.FormConfigBean;
import com.looseboxes.webform.services.MessageAttributesService;
import java.util.Collections;

/**
 * @author hp
 */
public class FormControllerHtml<T> extends FormControllerBase<T>{
    
    private final Logger log = LoggerFactory.getLogger(FormControllerHtml.class);
    
    @Autowired private Environment environment;
    @Autowired private FormEndpoints formEndpoints;
    @Autowired private MessageAttributesService messageAttributesService;

    public FormControllerHtml() { }

    @GetMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public String showForm(ModelMap model, FormConfigBean formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) 
            throws FileNotFoundException{

        super.onBeginForm(model, formConfigDTO, request, response);
        
        return this.formEndpoints.forCrudAction(formConfigDTO.getCrudAction());
    }
    
    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/"+FormStage.validate)
    public String validateForm(
            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) T modelobject,
            BindingResult bindingResult,
            ModelMap model,
            FormConfigBean formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) {
        
        super.onValidateForm(
                modelobject, bindingResult, model, formConfigDTO, request, response);
        
        final String target;
        
        if (bindingResult.hasErrors()) {
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            target = formEndpoints.getForm();
            
        }else{
            
            target = formEndpoints.getFormConfirmation();
        }

        return target;
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/"+FormStage.submit)
    public String submitForm(
            ModelMap model, FormConfigBean formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) {
        
        String target;
        try{
        
            super.onSubmitForm(model, formConfigDTO, request, response);
            
            target = getTargetAfterSubmit(formConfigDTO).orElse(getSuccessEndpoint());
            
        }catch(RuntimeException e) {

            target = this.getErrorEndpoint();
        }
        
        log.debug("Target: {}", target);
        
        return target;
    } 
    
    //////////////////////////////////////////////////////////////////////////
    // Exception handling
    // @see https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
    // 
    // We do not annotate these methods with @ExceptionHandler annotation so as
    // not to box in the user of this controller. Implementations of this class
    // are thus more flexible in deciding how to handle exceptions.
    //////////////////////////////////////////////////////////////////////////

    public ModelAndView handleMaxSizeExceeded(
        MaxUploadSizeExceededException exception, HttpServletRequest request) {

        // 1MB is the default, if none is set in properties file
        final String max = environment.getProperty(
                SpringProperties.MULTIPART_MAX_FILE_SIZE, "1MB");
        
        final List<String> errors = Collections.singletonList(
                "One or more files, you tried to upload exceeds the max of " + max);
        
        return this.handleException(exception, HttpStatus.PAYLOAD_TOO_LARGE, errors);
    }

    public ModelAndView handleResourceNotFound(
        ResourceNotFoundException exception, HttpServletRequest request) {
  
        final List<String> errors = Arrays.asList(
                "The page you request was not found",
                "It may have been moved, or it is no longer available",
                "Also, check that you entered the correct address in the browser.",
                "",
                request.getRequestURI(),
                "",
                "Meanwhile, keep calm, keep browsing");
        
        return this.handleException(exception, HttpStatus.NOT_FOUND, errors);
    }

    public ModelAndView handleRouteProblem(
            RouteException exception, HttpServletRequest request) {
  
        final List<String> errors = Arrays.asList(
                "Seems you took a wrong turn. The route you requested leads no where:",
                "",
                request.getRequestURI(),
                "",
                "Meanwhile, keep calm, keep browsing");

        return this.handleException(exception, HttpStatus.BAD_REQUEST, errors);
    }
    
    public ModelAndView handleException(Exception exception, HttpServletRequest request) {
    
        final List<String> errors = Arrays.asList(
                "We are unable to fullfill your request at this time.",
                "",
                request.getRequestURI(),
                "",
                "Meanwhile, keep calm, keep browsing");
        
        return this.handleException(exception, HttpStatus.INTERNAL_SERVER_ERROR, errors);
    }
            
    protected ModelAndView handleException(
            Exception exception, HttpStatus status, List<String> errors) {
    
        final String endpoint = this.getErrorEndpoint();
        
        final ModelAndView modelAndView = new ModelAndView(endpoint);
        
        this.messageAttributesService.addErrorMessage(modelAndView.getModel(), errors);

        modelAndView.setStatus(status);
        
        log.warn(errors.toString(), exception);

        return modelAndView;
    }

    private String getErrorEndpoint() {
        final String endpoint = this.formEndpoints.getError();
        return endpoint;
    }
    
    private String getSuccessEndpoint() {
        final String endpoint = this.formEndpoints.getSuccess();
        return endpoint;
    }
}
