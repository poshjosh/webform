package com.looseboxes.webform.web;

import com.looseboxes.webform.HttpSessionAttributes;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.WebRequest;

/**
 * @author hp
 */
@Component
public class WebValidator {
    
    @Autowired private Validator validator;
    
    public WebRequestDataBinder validate(WebRequest webRequest) {
        
        final Object modelobject = this.getModelObject(webRequest);
    
        final WebRequestDataBinder dataBinder = new WebRequestDataBinder(modelobject);
        
        dataBinder.setValidator(validator);

        dataBinder.bind(webRequest);

        dataBinder.validate();
        
        return dataBinder;
    }

    public WebDataBinder validateSingle(WebRequest webRequest, String name, Object value) {
        
        final Object modelobject = this.getModelObject(webRequest);
    
        return this.validateSingle(modelobject, name, value);
    }

    public WebDataBinder validateSingle(Object modelobject, String name, Object value) {
        
        final WebDataBinder dataBinder = new WebDataBinder(modelobject);
        dataBinder.setAllowedFields(name);
        
        dataBinder.setValidator(validator);

        dataBinder.bind(new MutablePropertyValues().add(name, value));

        dataBinder.validate();
        
        return dataBinder;
    }

    private Object getModelObject(WebRequest webRequest) {
        return webRequest.getAttribute(
                HttpSessionAttributes.MODELOBJECT, 
                org.springframework.web.context.request.WebRequest.SCOPE_SESSION);
    }
}
