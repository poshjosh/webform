package com.looseboxes.webform.converters;

import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author hp
 */
public interface IdStringToDomainTypeConverterFactory 
        extends ConverterFactory<String, Object>, ConditionalConverter{
    
}
