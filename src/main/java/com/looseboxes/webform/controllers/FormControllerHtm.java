package com.looseboxes.webform.controllers;

import com.looseboxes.webform.FormEndpoints;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.SpringProperties;
import com.looseboxes.webform.exceptions.RouteException;
import com.looseboxes.webform.exceptions.ResourceNotFoundException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import com.looseboxes.webform.web.FormConfigDTO;
import java.util.Collections;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import com.looseboxes.webform.FormStages;
import com.looseboxes.webform.web.ResponseHandler;

/**
 * @author hp
 */
public class FormControllerHtm<T> extends FormControllerBase<T>{
    
    private final Logger log = LoggerFactory.getLogger(FormControllerHtm.class);
    
    @Autowired private Environment environment;
    @Autowired private ResponseHandler<FormConfigDTO, String> responseHandler;
    @Autowired private FormEndpoints formEndpoints;

    public FormControllerHtm() { }

    @GetMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public String showForm(
            ModelMap model, FormConfigDTO formConfig,
            HttpServletRequest request, HttpServletResponse response){

        try{
            
            formConfig = super.onBeginForm(formConfig, request);
            
            return responseHandler.respond(formConfig);
            
        }catch(Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return responseHandler.respond(formConfig, e);
        }finally{
            this.addStatusAndMessages(model, formConfig, response);
        }
    }

    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + 
            FormStages.validate+"/" + FormStages.submit)
    public String validateThenSubmitForm(
            ModelMap model, FormConfigDTO formConfig,
            HttpServletRequest request, HttpServletResponse response, WebRequest webRequest) {
        
        try{
            
            formConfig = super.onValidateThenSubmitForm(formConfig, request, webRequest);
            
            return responseHandler.respond(formConfig);
            
        }catch(Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return responseHandler.respond(formConfig, e);
        }finally{
        
            this.addStatusAndMessages(model, formConfig, response);
        }
    }    
    
    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/"+FormStages.validate)
    public String validateForm(
            ModelMap model, FormConfigDTO formConfig,
            HttpServletRequest request, HttpServletResponse response, WebRequest webRequest) {
        
        try{
            
            formConfig = super.onValidateForm(formConfig, request, webRequest);

            return responseHandler.respond(formConfig);
            
        }catch(Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return responseHandler.respond(formConfig, e);
        }finally{
            this.addStatusAndMessages(model, formConfig, response);
        }
    }    
    
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/"+FormStages.submit)
    public String submitForm(
            ModelMap model, FormConfigDTO formConfig, 
            HttpServletRequest request, HttpServletResponse response) {
        
        try{
            
            formConfig = super.onSubmitForm(formConfig, request);

            return responseHandler.respond(formConfig);
            
        }catch(Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return responseHandler.respond(formConfig, e);
        }finally{
            this.addStatusAndMessages(model, formConfig, response);
        }
    } 

    private void addStatusAndMessages(ModelMap model, FormConfigDTO formConfig, HttpServletResponse response){
        if(formConfig == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            if(formConfig.hasErrors()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            model.put("FormConfig", formConfig);
        }
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
    
        final ModelAndView modelAndView = new ModelAndView(formEndpoints.getError());
        
        modelAndView.getModel().put("FormConfig", new FormConfigDTO().addErrorMessages(errors));

        modelAndView.setStatus(status);
        
        log.warn(errors.toString(), exception);

        return modelAndView;
    }
}
