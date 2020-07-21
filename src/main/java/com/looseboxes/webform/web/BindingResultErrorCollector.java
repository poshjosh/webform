package com.looseboxes.webform.web;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * @author hp
 */
public class BindingResultErrorCollector {
    
    private final Logger LOG = LoggerFactory.getLogger(BindingResultErrorCollector.class);
    
    public Collection<ValidationMessage> getErrors(BindingResult bindingResult) {
    
        if (bindingResult.hasErrors()) {

            final List<ObjectError> globalErrors = bindingResult.getGlobalErrors();
            this.log("GlobalError: ", globalErrors);
            
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            this.log(" FieldError: ", fieldErrors);
            
            final Set<ValidationMessage> allErrors = new LinkedHashSet();
            
            if(globalErrors != null && !globalErrors.isEmpty()) {
            
                final Function<ObjectError, ValidationMessage> mapper = this.getObjectErrorFormat();
                
                globalErrors.stream().map(mapper).collect(Collectors.toCollection(() -> allErrors));
            }
            
            if(fieldErrors != null) {
                
                final Function<FieldError, ValidationMessage> mapper = this.getFieldErrorFormat();
                
                fieldErrors.stream().map(mapper).collect(Collectors.toCollection(() -> allErrors));
            }
            
            return Collections.unmodifiableSet(allErrors);
            
        }else{
            
            return Collections.EMPTY_SET;
        }
    }

    public Set<ValidationMessage> getFieldErrors(BindingResult bindingResult, String fieldName) {
        return this.getFieldErrors(bindingResult, fieldName, this.getFieldErrorFormat());
    }
    
    public Set<ValidationMessage> getFieldErrors(BindingResult bindingResult, String fieldName, 
            Function<FieldError, ValidationMessage> converter) {
    
        if (bindingResult.hasFieldErrors(fieldName)) {
            
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors(fieldName);
            if(fieldErrors != null && !fieldErrors.isEmpty()) {
                this.log("FieldError: ", fieldErrors);
                return Collections.unmodifiableSet(fieldErrors.stream().map(converter).collect(Collectors.toSet()));
            }else{
                return Collections.EMPTY_SET;
            }
        }else{
            return Collections.EMPTY_SET;
        }
    }

    public Function<ObjectError, ValidationMessage> getObjectErrorFormat() {
        final Function<ObjectError, ValidationMessage> mapper = (objectErr) -> {
            final ValidationMessage err = new ValidationMessage();
            err.setMessage(objectErr.getDefaultMessage());
            err.setObjectName(objectErr.getObjectName());
            return err;
        };
        return mapper;
    }
    
    public Function<FieldError, ValidationMessage> getFieldErrorFormat() {
        final Function<FieldError, ValidationMessage> mapper = (fieldErr) -> {
            final ValidationMessage err = new ValidationMessage();
            err.setFieldName(fieldErr.getField());
            err.setMessage(fieldErr.isBindingFailure() ? 
                    "invalid format" : fieldErr.getDefaultMessage());
            err.setObjectName(fieldErr.getObjectName());
            err.setFieldValue(fieldErr.getRejectedValue());
            return err;
        };
        return mapper;
    }

    private void log(String prefix, List errors) {
        if(errors != null && ! errors.isEmpty()) {
            if(errors.size() == 1) {
                LOG.warn("{} {}",prefix, errors.get(0));
            }else{
                final Object log = errors.stream()
                        .map(err -> String.valueOf(err))
                        .collect(Collectors.joining("\n"+prefix, prefix, ""));
                LOG.warn("{}", log);
            }
        }
    }
}
