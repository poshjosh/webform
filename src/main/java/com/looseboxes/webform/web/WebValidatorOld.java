/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.looseboxes.webform.web;

import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.config.WebformMvcConfigurer;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebRequestDataBinder;

/**
 *
 * @author hp
 */
public class WebValidatorOld {
    
    private DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService() {
        @Override
        protected GenericConverter getConverter(
                TypeDescriptor sourceType, TypeDescriptor targetType) {
            final GenericConverter converter = super.getConverter(sourceType, targetType);
            System.out.println("WV - Source: " + sourceType.getName() + 
                    ", target: " + targetType.getName() + ", converter: " + converter);
            return converter;
        }
            
    };
    @Autowired private WebformMvcConfigurer configurer;
    @Autowired private Validator validator;
    private boolean added;
    
    public WebRequestDataBinder validate(org.springframework.web.context.request.WebRequest webRequest) {
        
        final Object modelobject = this.getModelObject(webRequest);
    
        System.out.println("WV - " + modelobject.getClass().getName());

        final WebRequestDataBinder dataBinder = new WebRequestDataBinder(modelobject);
        dataBinder.setConversionService(conversionService);
        if( ! added) {
            added = true;
        }
            configurer.addFormatters(conversionService);
        
        dataBinder.setValidator(validator);

        dataBinder.bind(webRequest);

        dataBinder.validate();
        
        return dataBinder;
    }

    public WebDataBinder validateSingle(org.springframework.web.context.request.WebRequest webRequest, String name, Object value) {
        
        final Object modelobject = this.getModelObject(webRequest);
    
        return this.validateSingle(modelobject, name, value);
    }

    public WebDataBinder validateSingle(Object modelobject, String name, Object value) {
        
        System.out.println("WV - " + modelobject.getClass().getName()+"#"+name+" = " + value);
        
        final WebDataBinder dataBinder = new WebDataBinder(modelobject);
        dataBinder.setAllowedFields(name);
        dataBinder.setConversionService(conversionService);
        if( ! added) {
            added = true;
            System.out.println("WV - Adding formatters");
        }
            configurer.addFormatters(conversionService);

        dataBinder.setValidator(validator);

        dataBinder.bind(new MutablePropertyValues().add(name, value));

        dataBinder.validate();
        
        return dataBinder;
    }

    private Object getModelObject(org.springframework.web.context.request.WebRequest webRequest) {
        return webRequest.getAttribute(
                HttpSessionAttributes.MODELOBJECT, 
                org.springframework.web.context.request.WebRequest.SCOPE_SESSION);
    }
}
