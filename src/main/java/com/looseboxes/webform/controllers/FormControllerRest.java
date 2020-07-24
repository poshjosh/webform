package com.looseboxes.webform.controllers;

import com.bc.webform.choices.SelectOption;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.web.FormConfigDTO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import com.looseboxes.webform.FormStages;
import com.looseboxes.webform.web.ResponseHandler;
import java.util.List;

/**
 * Override the {@link #getDependents(com.looseboxes.webform.web.FormConfigBean, java.lang.String)} 
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
    
    private final Logger log = LoggerFactory.getLogger(FormControllerRest.class);
    
    @Autowired private ResponseHandler<FormConfigDTO, ResponseEntity<FormConfigDTO>> responseService;
    
    public FormControllerRest() { }

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public ResponseEntity<FormConfigDTO> begin(
            FormConfigDTO formConfig, HttpServletRequest request) {
        try{
            
            formConfig = super.onBeginForm(formConfig, request);

            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(formConfig, e);
        }
    }

    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + 
            FormStages.validate + "/" + FormStages.submit)
    public ResponseEntity<FormConfigDTO> validateThenSubmit(FormConfigDTO formConfig,
            HttpServletRequest request, WebRequest webRequest) {
        try{
            
            formConfig = super.onValidateThenSubmitForm(formConfig, request, webRequest);
            
            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(formConfig, e);
        }
    }    
    
    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStages.validate)
    public ResponseEntity<FormConfigDTO> validate(FormConfigDTO formConfig,
            HttpServletRequest request, WebRequest webRequest) {
        try{
            
            formConfig = super.onValidateForm(formConfig, request, webRequest);

            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(formConfig, e);
        }
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStages.submit)
    public ResponseEntity<FormConfigDTO> submit(
            FormConfigDTO formConfig, HttpServletRequest request) {
        try{
            
            formConfig = super.onSubmitForm(formConfig, request);
            
            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {

            return responseService.respond(formConfig, e);
        }
    } 

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStages.dependents)
    public ResponseEntity<Object> dependents(
            @RequestParam(name = Params.FORMID, required = true) String formid, 
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            @RequestParam(name = "propertyValue", required = true) String propertyValue,
            HttpServletRequest request, HttpServletResponse response) {
        try{
            
            final Map<String, List<SelectOption>> result = super.onGetDependents(
                    formid, propertyName, propertyValue, request);
            
            return ResponseEntity.ok(result);
            
        }catch(Exception e) {
            
            log.warn("", e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStages.validateSingle)
    public ResponseEntity<FormConfigDTO> validateSingle(
            @RequestParam(name = Params.FORMID, required = true) String formid, 
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            @RequestParam(name = "propertyValue", required = true) String propertyValue,
            HttpServletRequest request) {
       
        FormConfigDTO formConfig = null;
        try{
            
            formConfig = super.onValidateSingle(
                    formid, propertyName, propertyValue, request);

            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(formConfig, e);
        }
    }
    
    public ResponseHandler getResponseService() {
        return responseService;
    }
}
/**
 * 
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.dependents)
    public ResponseEntity<FormConfigDTO> dependents(
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
    public ResponseEntity<FormConfigDTO> validateSingle(
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
    public ResponseEntity<FormConfigDTO> validateSingle(
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