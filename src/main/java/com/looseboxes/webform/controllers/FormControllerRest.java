package com.looseboxes.webform.controllers;

import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.looseboxes.webform.CrudAction;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.ModelAttributes;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.FormConfigDTO;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * @author hp
 */
public class FormControllerRest extends FormControllerBase{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormControllerRest.class);
    
    @Autowired private EntityRepositoryFactory repoFactory;
    
    public FormControllerRest() { }

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public ResponseEntity<FormConfig> begin(ModelMap model, FormConfigDTO formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) 
            throws FileNotFoundException{

        ResponseEntity<FormConfig> result;
        
        FormConfig formConfig = null;
        
        try{
            
            formConfig = super.onBeginForm(
                    model, formConfigDTO, request, response);

            if(formConfig == null) {
            
                result = ResponseEntity.badRequest().build();
                
            }else{
            
                result = ResponseEntity.ok(formConfig);
            }
        }catch(Exception e) {
        
            result = this.handleException(formConfig, e);
        }
        
        return result;
    }
    
    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/validate")
    public ResponseEntity<FormConfig> validate(
            @Valid @ModelAttribute(ModelAttributes.MODELOBJECT) Object modelobject,
            BindingResult bindingResult,
            ModelMap model,
            FormConfigDTO formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) {
        
        ResponseEntity<FormConfig> result;
        
        FormConfig formConfig = null;
        
        try{
            
            formConfig = super.onValidateForm(
                    modelobject, bindingResult, model, formConfigDTO, request, response);

            final HttpStatus status = bindingResult.hasErrors() ?
                    HttpStatus.BAD_REQUEST : HttpStatus.OK;

            result = ResponseEntity.status(status).body(formConfig);
            
        }catch(Exception e) {
        
            result = this.handleException(formConfig, e);
        }
        
        return result;
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/submit")
    public ResponseEntity<Object> submit(
            ModelMap model,
            FormConfigDTO formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) {
        
        final CrudAction action = formConfigDTO.getCrudAction();
        
        ResponseEntity<Object> result;
        
        FormConfig formConfig = null;
        
        try{
        
            formConfig = super.onSubmitForm(model, formConfigDTO, request, response);
            
            if(formConfig == null) {
            
                result = ResponseEntity.badRequest().build();
                
            }else{

                result = this.buildSuccessResponse(action, formConfig);
            }
        }catch(Exception e) {

            result = this.handleException(formConfig, e);
        }
        
        return result;
    } 
    
    public ResponseEntity<Object> buildSuccessResponse(
            CrudAction action, FormConfig formConfig) {
        
        final Object modelobject = Objects.requireNonNull(formConfig.getModelobject());
        
        final ResponseEntity<Object> result;
        
        switch(action) {
            case create:
                result = ResponseEntity.created(
                        buildURIForRead(formConfig)).body(modelobject);
                break;
            case read:
            case update:
                result = ResponseEntity.ok(modelobject);
                break;
            case delete:
                result = ResponseEntity.noContent().build();
                break;
            default:
                throw Errors.unexpected(action, (Object[])CrudAction.values());
        }
        
        return result;
    }
    
    public URI buildURIForRead(FormConfig formConfig) {
        final Object modelobject = formConfig.getModelobject();
        final Object id = repoFactory.forEntity(modelobject.getClass())
                .getIdOptional(modelobject).orElse(null);
        Objects.requireNonNull(id);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
          .path(this.buildPathForRead(formConfig))
          .buildAndExpand(id)
          .toUri();
        return uri;
    }
    
    public String buildPathForRead(FormConfig formConfig) {
        return "/" + CrudAction.read + "/" + formConfig.getModelname() + "/{"+Params.MODELID+"}";
    }
    
    public ResponseEntity handleException(FormConfig formConfig, Exception e) {
        LOG.warn(String.valueOf(formConfig), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
