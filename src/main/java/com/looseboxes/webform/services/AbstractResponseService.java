package com.looseboxes.webform.services;

import com.looseboxes.webform.web.FormConfig;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

/**
 * @author hp
 */
public abstract class AbstractResponseService<T> implements ResponseService<T>{
    
    private final Logger log = LoggerFactory.getLogger(AbstractResponseService.class);
    
    private final MessageAttributesService messageAttributesService;

    public AbstractResponseService(MessageAttributesService messageAttributesService) {
        this.messageAttributesService = Objects.requireNonNull(messageAttributesService);
    }
    
    @Override
    public abstract ResponseEntity<T> respond(FormConfig formConfig);
    
    @Override
    public ResponseEntity<Object> respond(
            BindingResult bindingResult, ModelMap model) {
        return this.respond(bindingResult, null, model, null);
    }
    
    @Override
    public ResponseEntity<Object> respond(BindingResult bindingResult, 
            String propertyName, ModelMap model) {
        return this.respond(bindingResult, model, null);
    }

    @Override
    public ResponseEntity<Object> respond(
            BindingResult bindingResult, 
            ModelMap model, Object payload) {
        return this.respond(bindingResult, null, model, payload);
    }

    @Override
    public ResponseEntity<Object> respond(
            BindingResult bindingResult, String propertyName,
            ModelMap model, Object payload) {
        if(propertyName == null ? bindingResult.hasErrors() : bindingResult.hasFieldErrors(propertyName)) {
            //Apparently, the ModelMap contained some custom Spring object which
            // jackson could not serialize, so we collect them into a map and 
            // serialize that
            final Object body = this.messageAttributesService.collectMessagesFromModelMap(model);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }else{
            log.trace("Response body: {}", payload);
            return payload == null ? ResponseEntity.ok().build() : ResponseEntity.ok(payload);
        }
    }
    
    @Override
    public ResponseEntity<Object> respond(Exception e, ModelMap model) {
        log.warn("Unexpected exception", e);
        this.messageAttributesService.addErrorMessage(model, "An unexpected error occured");
        //Apparently, the ModelMap contained some custom Spring object which
        // jackson could not serialize, so we collect them into a map and 
        // serialize that
        final Map body = this.messageAttributesService.collectMessagesFromModelMap(model);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
