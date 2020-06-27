package com.looseboxes.webform.controllers;

import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.FormConfigBean;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.form.DependentsProvider;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    private final Logger log = LoggerFactory.getLogger(FormControllerRest.class);
    
    @Autowired private EntityRepositoryFactory repoFactory;
    @Autowired private DependentsProvider dependentsProvider;
    @Autowired private DomainObjectPrinter domainObjectPrinter;
    
    public FormControllerRest() { }
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
            
            log.debug("#dependents {} = {}, session: {}", 
                    propertyName, propertyValue, request.getSession().getId());
            
            final FormConfigBean formConfig = findFormConfig(request, formid);
            
            this.log(FormStage.dependents, model, formConfig, request, response);
            
            final Object modelobject = formConfig.getModelobject();

            final Map<PropertyDescriptor, List> dependents = this.dependentsProvider
                    .getDependents(modelobject, propertyName, propertyValue);
            
            final Locale locale = request.getLocale();
            
            final Map<String, Map> result = this.getChoicesForDependents(dependents, locale);
            
            log.debug("{}#{} {} = {}", formConfig.getModelname(), 
                    propertyName, FormStage.dependents, result);
            
            return ResponseEntity.ok(result);
            
        }catch(Exception e) {
            return this.respond(e, model);
        }
    }
    
    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.validateSingle)
    public ResponseEntity<Object> validateSingle(
            @Valid @ModelAttribute(HttpSessionAttributes.MODELOBJECT) Object modelobject, 
            BindingResult bindingResult, 
            ModelMap model, 
            @RequestParam(name = Params.FORMID, required = true) String formid, 
            @RequestParam(name = "propertyName", required = true) String propertyName, 
            @RequestParam(name = "propertyValue", required = true) String propertyValue,
            HttpServletRequest request, HttpServletResponse response) {
        
        try{
            
            log.debug("#validateSingle {} = {}, session: {}", 
                    propertyName, propertyValue, request.getSession().getId());

            final FormConfigBean formConfig = findFormConfig(request, formid);
            
            this.log(FormStage.validateSingle, model, formConfig, request, response);
            
//            final Object modelobject = formConfig.getModelobject();
            
//            final BindingResult bindingResult = 
//                    this.validateSingle(modelobject, propertyName, propertyValue);
                    
            if(bindingResult.hasFieldErrors(propertyName)) {
            
                this.getMessageAttributesSvc()
                        .addErrorToModel(bindingResult, model, propertyName);
                
            }else{
            
                this.validateModelObject(
                        bindingResult, model, formConfig, modelobject);
            }
            
            return this.respond(bindingResult, propertyName, model, formConfig);
            
        }catch(Exception e) {
            return this.respond(e, model);
        }
    }
    
    /**
     * Return a valid {@link com.looseboxes.webform.form.FormConfigBean} for
     * the formid argument.
     * 
     * The formConfig passed via the request is not initialized with a 
     * form i.e FormConfig.getForm as well as FormConfig.getModelobject 
     * will return null. To check that the FormConig refers to a valid
     * instance, we use FormConfig.getFid() to search for a corresponding
     * session attribute; which should return an initialized instance
     * @param request
     * @param formid
     * @return 
     */
    private FormConfigBean findFormConfig(HttpServletRequest request, String formid) {
        final FormConfigBean formConfig = 
                (FormConfigBean)request.getSession().getAttribute(formid);
        if(formConfig == null) {
            throw new InvalidRouteException();
        }
        log.trace("Found: {}", formConfig);
        return formConfig;
    }

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}")
    public ResponseEntity<Object> begin(
            ModelMap model, FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response) 
            throws FileNotFoundException{

        ResponseEntity<Object> result;
        
        FormConfig formConfig = null;
        
        try{
            
            formConfig = super.onBeginForm(
                    model, formConfigBean, request, response);

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
            ModelMap model, FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response) {
        
        ResponseEntity<Object> result;
        
        FormConfig formConfig = null;
        
        try{
            
            formConfig = super.onValidateForm(
                    modelobject, bindingResult, model, formConfigBean, request, response);

            result = this.respond(bindingResult, model, formConfig);
            
        }catch(Exception e) {
        
            result = this.respond(e, model);
        }
        
        return result;
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/" + FormStage.submit)
    public ResponseEntity<Object> submit(
            ModelMap model, FormConfigBean formConfigBean,
            HttpServletRequest request, HttpServletResponse response) {
        
        final CRUDAction action = formConfigBean.getCrudAction();
        
        ResponseEntity<Object> result;
        
        FormConfig formConfig = null;
        
        try{
        
            formConfig = super.onSubmitForm(model, formConfigBean, request, response);
            
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
    
    protected Map<String, Map> getChoicesForDependents(
            Map<PropertyDescriptor, List> dependents, Locale locale) {
    
        final Map<String, Map> result = new HashMap(dependents.size(), 1.0f);

        dependents.forEach((propertyDescriptor, entityList) -> {
            final Map choices = new HashMap(entityList.size(), 1.0f);
            final Class entityType = propertyDescriptor.getPropertyType();
            final EntityRepository repo = repoFactory.forEntity(entityType);
            for(Object entity : entityList) {
                final Object key = repo.getIdOptional(entity).orElse(null);
                Objects.requireNonNull(key);
                final Object val = this.domainObjectPrinter.print(entity, locale);
                Objects.requireNonNull(val);
                choices.put(key, val);
            }
            final String name = propertyDescriptor.getName();
            result.put(name, choices);
        });
        
        return result;
    }
    
    protected ResponseEntity<Object> buildSuccessResponse(
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
    
    protected URI buildURIForRead(FormConfig formConfig) {
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
    
    protected String buildPathForRead(FormConfig formConfig) {
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
            log.trace("Response body: {}", payload);
            return payload == null ? ResponseEntity.ok().build() : ResponseEntity.ok(payload);
        }
    }
    
    protected ResponseEntity<Object> respond(Exception e, ModelMap model) {
        log.warn("Unexpected exception", e);
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
