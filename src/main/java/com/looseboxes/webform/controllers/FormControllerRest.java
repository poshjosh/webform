package com.looseboxes.webform.controllers;

import com.looseboxes.webform.Params;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigBean;
import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.HttpSessionAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import com.looseboxes.webform.services.ResponseService;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Override the {@link #getDependents(com.looseboxes.webform.form.FormConfigDTO, java.lang.String)} 
 * method to provide dependents for a current form input.
 * 
 * For example if an Address Form has both country and region inputs, and
 * the region input is dependent on the country input. Then, when the 
 * country input is selected, use this method to return the list of regions
 * for the selected country. The regions returned will immediately be
 * rendered giving the user an option to select from.
 * @author hp
 */
public class FormControllerRest<T> extends FormControllerBase<T>{
    
//    private final Logger log = LoggerFactory.getLogger(FormControllerRest.class);
    
    @Autowired private ResponseService responseService;
    
    public FormControllerRest() { }

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public ResponseEntity<Object> begin(
            ModelMap model, FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response) 
            throws FileNotFoundException{
        try{
            
            final FormConfig formConfig = super.onBeginForm(
                    model, formConfigBean, request, response);

            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(e, model);
        }
    }
    
    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.validate)
    public ResponseEntity<Object> validate(
            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) T modelobject,
            BindingResult bindingResult,
            ModelMap model, FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response) {
        try{
            
            final FormConfig formConfig = super.onValidateForm(
                    modelobject, bindingResult, model, formConfigBean, request, response);

            return this.responseService.respond(bindingResult, model, formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(e, model);
        }
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.submit)
    public ResponseEntity<Object> submit(
            ModelMap model, FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response) {
        try{
        
            final FormConfig formConfig = super.onSubmitForm(
                    model, formConfigBean, request, response);
            
            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {

            return responseService.respond(e, model);
        }
    } 

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.dependents)
    public ResponseEntity<Object> dependents(
            // Without the @Valid annotation the modelobject properties were not set
            // With the @Valid annotation the property values must be valid or error:
            // InvocableHandlerMethod: Could not resolve parameter [0] in {METHOD_SIGNATURE}: org.springframework.validation.BeanPropertyBindingResult: 3 errors
//            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) Object modelobject, 
            ModelMap model, 
            @RequestParam(name = Params.FORMID, required = true) String formid, 
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            @RequestParam(name = "propertyValue", required = true) String propertyValue,
            HttpServletRequest request, HttpServletResponse response) {
        try{
            
            final Map<String, Map> result = super.onGetDependents(
                    model, formid, propertyName, propertyValue, request, response);
            
            return ResponseEntity.ok(result);
            
        }catch(Exception e) {
            
            return this.responseService.respond(e, model);
        }
    }
    
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.validateSingle)
    public ResponseEntity<Object> validateSingle(
            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) T modelobject, 
            BindingResult bindingResult, 
            ModelMap model, 
            @RequestParam(name = Params.FORMID, required = true) String formid, 
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            @RequestParam(name = "propertyValue", required = true) String propertyValue,
            HttpServletRequest request, HttpServletResponse response) {
       try{
            
            final FormConfig formConfig = super.onValidateSingle(modelobject, bindingResult, 
                    model, formid, propertyName, propertyValue, request, response);

            return this.responseService.respond(
                    bindingResult, propertyName, model, formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(e, model);
        }
    }
    
    public ResponseService getResponseService() {
        return responseService;
    }
}
/**
 * 
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.dependents)
    public ResponseEntity<Object> dependents(
            // Without the @Valid annotation the modelobject properties were not set
            // With the @Valid annotation the property values must be valid or error:
            // InvocableHandlerMethod: Could not resolve parameter [0] in {METHOD_SIGNATURE}: org.springframework.validation.BeanPropertyBindingResult: 3 errors
//            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) Object modelobject, 
            ModelMap model, 
            @RequestParam(name = Params.FORMID, required = true) String formid, 
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            @RequestParam(name = "propertyValue", required = true) String propertyValue,
            HttpServletRequest request, HttpServletResponse response) {
        try{
            
            final Map<String, Map> result = super.onGetDependents(
                    model, formid, propertyName, propertyValue, request, response);
            
            return ResponseEntity.ok(result);
            
        }catch(Exception e) {
            
            return this.responseService.respond(e, model);
        }
    }
    
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.validateSingle)
    public ResponseEntity<Object> validateSingle(
            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) T modelobject, 
            BindingResult bindingResult, 
            ModelMap model, 
            @RequestParam(name = Params.FORMID, required = true) String formid, 
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            @RequestParam(name = "propertyValue", required = true) String propertyValue,
            HttpServletRequest request, HttpServletResponse response) {
       try{
            
            final FormConfig formConfig = super.onValidateSingle(modelobject, bindingResult, 
                    model, formid, propertyName, propertyValue, request, response);

            return this.responseService.respond(
                    bindingResult, propertyName, model, formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(e, model);
        }
    }

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.validateSingle)
    public ResponseEntity<Object> validateSingle(
            ModelMap model, 
            @RequestParam(name = Params.FORMID, required = true) String formid, 
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            @RequestParam(name = "propertyValue", required = true) String propertyValue,
            WebRequest webRequest) {
       try{
           
            final WebDataBinder dataBinder = this.webValidator
                    .validateSingle(webRequest, propertyName, propertyValue);
            
            final BindingResult bindingResult = dataBinder.getBindingResult();
            
            log.debug("#onValidateSingle {} = {}, session: {}", 
                    propertyName, propertyValue, webRequest.getSessionId());

            FormConfig formConfig = this.findFormConfig(webRequest, formid);

            final Object modelobject = dataBinder.getTarget();

            formConfig = this.getService().validateSingle(modelobject, bindingResult, 
                    model, formConfig, propertyName, propertyValue);

            return this.responseService.respond(
                    bindingResult, propertyName, model, formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(e, model);
        }
    }

 * 
 */