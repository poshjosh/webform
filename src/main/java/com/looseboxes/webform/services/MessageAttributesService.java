package com.looseboxes.webform.services;

import com.looseboxes.webform.Errors;
import com.looseboxes.webform.MessageAttributes;
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

/**
 * @author hp
 */
@Service
public class MessageAttributesService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageAttributesService.class);


    private final MessageAttributes messageAttributes;
    
    @Autowired
    public MessageAttributesService(MessageAttributes messageAttributes) { 
        this.messageAttributes = Objects.requireNonNull(messageAttributes);
    }
    
    public void addErrorsToModel(BindingResult bindingResult, Object model) {
    
        if (bindingResult.hasErrors()) {

            this.log("GlobalError:: ", bindingResult.getGlobalErrors());
            this.log("FieldError:: ", bindingResult.getFieldErrors());
            
            //@TODO Add global errors
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            if(fieldErrors != null) {
                
                final Function<FieldError, String> mapper = this.getFieldErrorFormat();

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
    
    private void log(String prefix, List errors) {
        if(errors != null) {
            if(errors.size() == 1) {
                LOG.warn("{} {}",prefix, errors.get(0));
            }else{
                final Object log = errors.stream()
                        .collect(Collectors.joining("\n"+prefix, prefix, ""));
                LOG.warn("{}", log);
            }
        }
    }

    public void addErrorToModel(BindingResult bindingResult, Object model, String fieldName) {
        this.addErrorToModel(bindingResult, model, fieldName, this.getFieldErrorFormat());
    }
    
    public void addErrorToModel(BindingResult bindingResult, Object model, 
            String fieldName, Function<FieldError, String> mapper) {
    
        if (bindingResult.hasFieldErrors(fieldName)) {
            
            final FieldError fieldError = bindingResult.getFieldError(fieldName);
            if(fieldError != null) {
                LOG.warn("FieldError:: ", fieldError);
                this.addErrorMessage(model, mapper.apply(fieldError));
            }
        }    
    }
    
    public Function<FieldError, String> getFieldErrorFormat() {
        final Function<FieldError, String> mapper = (fieldErr) -> {

            final String errMsg;
            if(fieldErr.isBindingFailure()) {
                errMsg = "internal error";
            }else{
                errMsg = fieldErr.getDefaultMessage();
            }

            return fieldErr.getField() + (errMsg == null ? "" : ": " + errMsg);
        };
        return mapper;
    }
    
    public void addInfoMessage(Object model, Object value) {
        this.addCollectionAttribute(model, getInfoMessageAttribute(), value);
    }

    public void addInfoMessages(Object model, Object... messages) {
        this.addInfoMessages(model, Arrays.asList(messages));
    }
    
    public void addInfoMessages(Object model, Collection messages) {
        this.addCollectionAttribute(model, getInfoMessageAttribute(), messages);
    }

    public void addErrorMessage(Object model, Object value) {
        this.addCollectionAttribute(model, getErrorMessageAttribute(), value);
    }

    public void addErrorMessages(Object model, Object... messages) {
        this.addErrorMessages(model, Arrays.asList(messages));
    }
    
    public void addErrorMessages(Object model, Collection messages) {
        this.addCollectionAttribute(model, getErrorMessageAttribute(), messages);
    }

    public void addCollectionAttribute(Object model, String name, Object value) {
        Objects.requireNonNull(model);
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        final Map m;
        if(model instanceof Model) {
            m = ((Model)model).asMap();
        }else if(model instanceof ModelMap){
            m = (ModelMap)model;
        }else{
            throw Errors.modelOrModelMapRequired(model.getClass());
        }
        
        Collection collection = (Collection)m.get(name);
        
        LOG.trace("Existing: {} = {}", name, collection);
        
        if(collection == null) {
            collection = new ArrayList();
            m.put(name, collection);
        }
        
        LOG.trace("For: {}, adding: {} to: {}", name, value, collection);

        if(value instanceof Collection) {
            
            collection.addAll((Collection)value);
            
        }else{
        
            collection.add(value);
        }
    }
    
    public String getErrorMessageAttribute() {
        return this.getMessageAttributes().getErrorMessages();
    }

    public String getInfoMessageAttribute() {
        return this.getMessageAttributes().getInfoMessages();
    }

    public MessageAttributes getMessageAttributes() {
        return messageAttributes;
    }
}
