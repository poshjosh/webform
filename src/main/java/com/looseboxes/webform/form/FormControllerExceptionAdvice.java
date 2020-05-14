package com.looseboxes.webform.form;

import com.looseboxes.webform.SpringProperties;
import com.looseboxes.webform.Templates;
import com.looseboxes.webform.services.MessageAttributesService;
import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author hp
 */
//The annotations below didn't work. spring-boot-starter-parent 2.2.7.RELEASE
//This class did not handle any of the declared exceptions
//@ControllerAdvice(assignableTypes = FormController.class)
//@ControllerAdvice(basePackageClasses = {FormController.class})
@ControllerAdvice 
public class FormControllerExceptionAdvice {
    
    private static final Logger LOG = LoggerFactory.getLogger(FormControllerExceptionAdvice.class);
    
    @Autowired private MessageAttributesService messageAttributesSvc;
    @Autowired private Environment env;
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSizeExceeded(
        MaxUploadSizeExceededException exception, 
        HttpServletRequest request,  
        HttpServletResponse response) {

// when form value entered, referer format:  http://.../create/blog     
// RequestURI for above referer:             http://.../create/blog/validate
//        final String referer = request.getHeader("referer");

        final ModelAndView modelAndView = new ModelAndView(Templates.ERROR);

        final String max = env.getProperty(
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
    
    
    // When we re-directed to say the form page, the entire gamut of attributes
    // required for that page to work was missing. Errors everwhere
    private String didnt_work_read_comments_getTarget(HttpServletRequest request, String resultIfNone){
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
}
