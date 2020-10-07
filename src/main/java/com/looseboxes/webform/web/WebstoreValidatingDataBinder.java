package com.looseboxes.webform.web;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.WebRequest;

/**
 * @author hp
 */
public class WebstoreValidatingDataBinder {
    
    private final Logger log = LoggerFactory.getLogger(WebstoreValidatingDataBinder.class);
    
    private final ConversionService conversionService;
    private final Validator validator;

    public WebstoreValidatingDataBinder(ConversionService conversionService, Validator validator) {
        this.conversionService = Objects.requireNonNull(conversionService);
        this.validator = Objects.requireNonNull(validator);
    }
    
    public DataBinder bindAndValidate(WebRequest webRequest, Object modelobject) {

        final DataBinder dataBinder = this.bind(webRequest, modelobject);
        
        return this.validate(dataBinder, modelobject);
    }
    
    public DataBinder bind(WebRequest webRequest, Object modelobject) {

        log.trace("BEFORE Modelobject: {}", modelobject);
        
        final WebRequestDataBinder dataBinder = new WebRequestDataBinder(modelobject);
        dataBinder.setConversionService(conversionService);

        dataBinder.bind(webRequest); 

        log.debug(" AFTER Modelobject: {}", modelobject);
        
        return dataBinder;
    }

    public DataBinder validate(DataBinder dataBinder, Object modelobject) {

        dataBinder.setValidator(validator);

        dataBinder.validate();
        
        log.trace("Validation result: {}", dataBinder.getBindingResult());
        
        return dataBinder;
    }
    
    public WebDataBinder bindAndValidateSingle(Object modelobject, String name, Object value) {
        
        final WebDataBinder dataBinder = new WebDataBinder(modelobject);
        dataBinder.setAllowedFields(name);
        dataBinder.setConversionService(conversionService);

        dataBinder.setValidator(validator);

        dataBinder.bind(new MutablePropertyValues().add(name, value));

        dataBinder.validate();
        
        return dataBinder;
    }
}
