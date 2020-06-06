package com.looseboxes.webform.controllers;

import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.FormConfigDTO;
import com.looseboxes.webform.util.Print;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Collections;
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
import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.HttpSessionAttributes;
import java.util.LinkedHashMap;
import java.util.List;
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
public class FormControllerRest extends FormControllerBase{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormControllerRest.class);
    
    @Autowired private EntityRepositoryFactory repoFactory;
    
    public FormControllerRest() { }
    
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.dependents)
    public ResponseEntity<Object> dependents(
            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) Object modelobject, 
            ModelMap model, FormConfigDTO formConfigDTO,
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            HttpServletRequest request, HttpServletResponse response) {

        try{
            
            if(LOG.isTraceEnabled()) {
                new Print().trace(FormStage.dependents, 
                        model, formConfigDTO, request, response);
            }

            formConfigDTO.setModelobject(modelobject);

            final Map dependents = this.getDependents(formConfigDTO, propertyName);

            return ResponseEntity.ok(
                    Collections.singletonMap(FormStage.dependents, dependents));
            
        }catch(Exception e) {
            return this.respond(e, model);
        }
    }
    
    /**
     * Override this method to provide dependents for a current form input.
     * 
     * For example if an Address Form has both country and region inputs, and
     * the region input is dependent on the country input. Then, when the 
     * country input is selected, use this method to return the list of regions
     * for the selected country. The regions returned will immediately be
     * rendered giving the user an option to select from.
     * 
     * @param formConfig
     * @param propertyName
     * @return 
     */
    public Map<String, List<Object>> getDependents(
            FormConfigDTO formConfig, String propertyName) {

        return Collections.EMPTY_MAP;
    }

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.validateSingle)
    public ResponseEntity<Object> validateSingle(
            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) Object modelobject, 
            BindingResult bindingResult,
            ModelMap model, FormConfigDTO formConfigDTO,
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            HttpServletRequest request, HttpServletResponse response) {
        
        try{
            
            if(LOG.isTraceEnabled()) {
                new Print().trace(FormStage.validateSingle, 
                        model, formConfigDTO, request, response);
            }
            
            if(bindingResult.hasFieldErrors(propertyName)) {
            
                this.getMessageAttributesSvc()
                        .addErrorToModel(bindingResult, model, propertyName);
                
            }else{
            
                formConfigDTO.setModelobject(modelobject);

                this.validateModelObject(bindingResult, model, formConfigDTO);
            }
            
            return this.respond(bindingResult, propertyName, model, formConfigDTO);
            
        }catch(Exception e) {
            return this.respond(e, model);
        }
    }

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public ResponseEntity<Object> begin(
            ModelMap model, FormConfigDTO formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) 
            throws FileNotFoundException{

        ResponseEntity<Object> result;
        
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
        
            result = this.respond(e, model);
        }
        
        return result;
    }
    
    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.validate)
    public ResponseEntity<Object> validate(
            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) Object modelobject,
            BindingResult bindingResult,
            ModelMap model, FormConfigDTO formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) {
        
        ResponseEntity<Object> result;
        
        FormConfig formConfig = null;
        
        try{
            
            formConfig = super.onValidateForm(
                    modelobject, bindingResult, model, formConfigDTO, request, response);

            result = this.respond(bindingResult, model, formConfig);
            
        }catch(Exception e) {
        
            result = this.respond(e, model);
        }
        
        return result;
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.submit)
    public ResponseEntity<Object> submit(
            ModelMap model, FormConfigDTO formConfigDTO,
            HttpServletRequest request, HttpServletResponse response) {
        
        final CRUDAction action = formConfigDTO.getCrudAction();
        
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

            result = this.respond(e, model);
        }
        
        return result;
    } 
    
    public ResponseEntity<Object> buildSuccessResponse(
            CRUDAction action, FormConfig formConfig) {
        
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
                throw Errors.unexpected(action, (Object[])CRUDAction.values());
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
        return "/" + CRUDAction.read + "/" + formConfig.getModelname() + "/{"+Params.MODELID+"}";
    }
    
    protected ResponseEntity<Object> respond(
            BindingResult bindingResult, ModelMap model) {
        return this.respond(bindingResult, null, model, null);
    }
    
    protected ResponseEntity<Object> respond(BindingResult bindingResult, 
            String propertyName, ModelMap model) {
        return this.respond(bindingResult, model, null);
    }

    protected ResponseEntity<Object> respond(
            BindingResult bindingResult, 
            ModelMap model, Object payload) {
        return this.respond(bindingResult, null, model, payload);
    }

    protected ResponseEntity<Object> respond(
            BindingResult bindingResult, String propertyName,
            ModelMap model, Object payload) {
        if(propertyName == null ? bindingResult.hasErrors() : bindingResult.hasFieldErrors(propertyName)) {
            final Object body = this.collectMessages(model);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }else{
            return payload == null ? ResponseEntity.ok().build() : ResponseEntity.ok(payload);
        }
    }
    
    protected ResponseEntity<Object> respond(Exception e, ModelMap model) {
        LOG.warn("Unexpected exception", e);
        getMessageAttributesSvc().addErrorMessage(model, "An unexpected error occured");
        final Map body = this.collectMessages(model);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * @param model
     * @return 
     * @see #collectMessages(org.springframework.ui.ModelMap, java.util.Map) 
     */
    protected Map collectMessages(ModelMap model) {
        return this.collectMessages(model, new LinkedHashMap());
    }
    
    /**
     * <p>
     * Collect the messages from the ModelMap.When we returned the ModelMap directly exception:
     * </p>
     * <code>com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class org.springframework.validation.DefaultMessageCodesResolver and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: java.util.HashMap["org.springframework.validation.BindingResult.formConfigDTO"]->org.springframework.validation.BeanPropertyBindingResult["messageCodesResolver"])</code>
     * <p>
     *    Apparently, the ModelMap contained some custom Spring object which
     *    jackson could not serialize
     * </p>
     * @param model
     * @param collectInto
     * @return 
     */
    protected Map collectMessages(ModelMap model, Map collectInto) {
        final String errorMsgAttr = this.getMessageAttributesSvc().getErrorMessageAttribute();
        collectInto.put(errorMsgAttr, model.getAttribute(errorMsgAttr));
        final String infoMsgAttr = this.getMessageAttributesSvc().getErrorMessageAttribute();
        collectInto.put(infoMsgAttr, model.getAttribute(infoMsgAttr));
        return collectInto;
    }
}
