package com.looseboxes.webform.services;

import com.looseboxes.webform.Errors;
import com.looseboxes.webform.ModelAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * @author hp
 */
@Service
public class MessageAttributesService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageAttributesService.class);

    @Autowired
    public MessageAttributesService() { }
    
    public void addErrorsToModel(BindingResult bindingResult, Object model) {
    
        if (bindingResult.hasErrors()) {
            
            final List<ObjectError> errors = bindingResult.getAllErrors();
            if(errors != null) {
                for(ObjectError err : errors) {
                    LOG.warn("ObjectError:: {}", err);
                }
            }
            
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            if(fieldErrors != null) {
                
                final Function<FieldError, String> mapper = (fieldErr) -> {
                    
                    return fieldErr.getField() + (fieldErr.getDefaultMessage() == null ? "" : ": " + fieldErr.getDefaultMessage());
                };

                final List<String> errorMessages = (List<String>)fieldErrors.stream()
                        .map(mapper).collect(Collectors.toCollection(() -> new ArrayList()));

                if(errorMessages.size() > 1) {
                    errorMessages.add(0, "The following field(s) have errors");
                }
                
                addErrorMessages(model, errorMessages);
                
            }else{
                
                addErrorMessage(model, "Unexpeted error occured while processing");
            }
        }
    }
    
    public void addInfoMessage(Object model, Object value) {
        this.addCollectionAttribute(model, ModelAttributes.MESSAGES, value);
    }

    public void addInfoMessages(Object model, Object... messages) {
        this.addInfoMessages(model, Arrays.asList(messages));
    }
    
    public void addInfoMessages(Object model, Collection messages) {
        this.addCollectionAttribute(model, ModelAttributes.MESSAGES, messages);
    }

    public void addErrorMessage(Object model, Object value) {
        this.addCollectionAttribute(model, ModelAttributes.ERRORS, value);
    }

    public void addErrorMessages(Object model, Object... messages) {
        this.addErrorMessages(model, Arrays.asList(messages));
    }
    
    public void addErrorMessages(Object model, Collection messages) {
        this.addCollectionAttribute(model, ModelAttributes.ERRORS, messages);
    }

    public void addCollectionAttribute(Object model, String name, Object value) {
        Objects.requireNonNull(model);
        Objects.requireNonNull(name);
        final Map m;
        if(model instanceof Model) {
            m = ((Model)model).asMap();
        }else if(model instanceof ModelMap){
            m = (ModelMap)model;
        }else{
            throw Errors.modelOrModelMapRequired(model.getClass());
        }
        
        Collection c = (Collection)m.get(name);
        
        LOG.trace("Existing: {} = {}", name, c);
        
        boolean added = false;
        
        if(c == null) {
            if(value instanceof Collection) {
                c = (Collection)value;
            }else{
                c = new ArrayList();
            }
            if(model instanceof Model) {
                ((Model)model).addAttribute(name, c);
                added = value instanceof Collection;
            }else if(model instanceof ModelMap){
                ((ModelMap)model).addAttribute(name, c);
                added = value instanceof Collection;
            }    
            if(added) {
                LOG.trace("For: {}, added: {}", name, value);
            }
        }
        
        if( ! added) {
            
            LOG.trace("For: {}, adding value: {} to: {}", name, value, c);
            
            c.add(value);
        }
    }
}
