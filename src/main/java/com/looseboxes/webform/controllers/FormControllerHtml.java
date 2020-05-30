package com.looseboxes.webform.controllers;

import com.looseboxes.webform.FormEndpoints;
import com.looseboxes.webform.ModelAttributes;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.SpringProperties;
import com.looseboxes.webform.exceptions.RouteException;
import com.looseboxes.webform.exceptions.TargetNotFoundException;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import com.looseboxes.webform.form.FormConfigDTO;

/**
 * @author hp
 */
public class FormControllerHtml extends FormControllerBase{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormControllerHtml.class);
    
    @Autowired private Environment environment;
    @Autowired private FormEndpoints formEndpoints;

    public FormControllerHtml() { }

    @GetMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public String showForm(ModelMap model, FormConfigDTO formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) 
            throws FileNotFoundException{

        super.onBeginForm(model, formConfigDTO, request, response);
        
        return this.formEndpoints.forCrudAction(formConfigDTO.getCrudAction());
    }
    
    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/validate")
    public String validateForm(
            @Valid @ModelAttribute(ModelAttributes.MODELOBJECT) Object modelobject,
            BindingResult bindingResult,
            ModelMap model,
            FormConfigDTO formConfigDTO,
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

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/submit")
    public String submitForm(
            ModelMap model,
            FormConfigDTO formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) {
        
        String target;
        try{
        
            super.onSubmitForm(model, formConfigDTO, request, response);
            
            target = getTargetAfterSubmit(formConfigDTO).orElse(getSuccessEndpoint());
            
        }catch(RuntimeException e) {

            target = this.getErrorEndpoint();
        }
        
        LOG.debug("Target: {}", target);
        
        return target;
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
        
        this.getMessageAttributesSvc().addErrorMessage(modelAndView.getModel(), error);
        
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
        
        this.getMessageAttributesSvc().addErrorMessage(modelAndView.getModel(), errors);

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
        
        this.getMessageAttributesSvc().addErrorMessage(modelAndView.getModel(), errors);

        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        
        LOG.warn(errors.toString(), exception);

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
