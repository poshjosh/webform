package com.looseboxes.webform.controllers;

import com.bc.webform.choices.SelectOption;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.web.FormConfigDTO;
import javax.servlet.http.HttpServletRequest;
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
 * @author hp
 */
public class FormControllerRest<T> extends FormControllerBase<T>{
    
    private final Logger log = LoggerFactory.getLogger(FormControllerRest.class);
    
    @Autowired private ResponseHandler<FormConfigDTO, ResponseEntity<FormConfigDTO>> responseService;
    
    public FormControllerRest() { }

    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + 
            FormStages.begin + "/" + FormStages.validate + "/" + FormStages.submit)
    public ResponseEntity<FormConfigDTO> beginThenValidateThenSubmit(
            FormConfigDTO formConfig, HttpServletRequest request, WebRequest webRequest) {
        try{
            
            formConfig = this.onBeginThenValidateThenSubmitForm(formConfig, request, webRequest);
            
            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(formConfig, e);
        }
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public ResponseEntity<FormConfigDTO> begin(FormConfigDTO formConfig, HttpServletRequest request) {
        
        try{
            
            formConfig = this.onBeginForm(formConfig, request);

            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(formConfig, e);
        }
    }

    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + 
            FormStages.validate + "/" + FormStages.submit)
    public ResponseEntity<FormConfigDTO> validateThenSubmit(
            FormConfigDTO formConfig, HttpServletRequest request, WebRequest webRequest) {
        try{
            
            formConfig = this.onValidateThenSubmitForm(formConfig, request, webRequest);
            
            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(formConfig, e);
        }
    }    
    
    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStages.validate)
    public ResponseEntity<FormConfigDTO> validate(
            FormConfigDTO formConfig, HttpServletRequest request, WebRequest webRequest) {
        try{
            
            formConfig = this.onValidateForm(formConfig, request, webRequest);

            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {
        
            return this.responseService.respond(formConfig, e);
        }
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStages.submit)
    public ResponseEntity<FormConfigDTO> submit(
            FormConfigDTO formConfig, HttpServletRequest request) {
        try{
            
            formConfig = this.onSubmitForm(formConfig, request);
            
            return this.responseService.respond(formConfig);
            
        }catch(Exception e) {

            return responseService.respond(formConfig, e);
        }
    } 

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStages.dependents)
    public ResponseEntity<Object> dependents(
            @RequestParam(name = Params.FORMID) String formid,
            @RequestParam(name = "propertyName") String propertyName,
            @RequestParam(name = "propertyValue") String propertyValue,
            HttpServletRequest request) {
        try{
            
            final Map<String, List<SelectOption>> result = this.onGetDependents(
                    formid, propertyName, propertyValue, request);
            
            return ResponseEntity.ok(result);
            
        }catch(Exception e) {
            
            log.warn("", e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStages.validateSingle)
    public ResponseEntity<FormConfigDTO> validateSingle(
            @RequestParam(name = Params.FORMID) String formid,
            @RequestParam(name = "propertyName") String propertyName,
            @RequestParam(name = "propertyValue") String propertyValue,
            HttpServletRequest request) {
       
        FormConfigDTO formConfig = null;
        try{
            
            formConfig = this.onValidateSingle(
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
