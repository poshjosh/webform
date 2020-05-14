package com.looseboxes.webform.converters;

import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author hp
 */
public class DomainObjectParserImpl implements DomainObjectParser{

    private static final Logger LOG = LoggerFactory.getLogger(DomainObjectParserImpl.class);
    
    private final Class [] domainClasses;
    
    private final ConverterFactory<String, Object> converterFactory;

    public DomainObjectParserImpl(
            ConverterFactory<String, Object> converterFactory, 
            Class [] domainClasses) {
        this.converterFactory = Objects.requireNonNull(converterFactory);
        this.domainClasses = Objects.requireNonNull(domainClasses);
    }
    
    @Override
    public Object parse(String text, Locale locale) throws ParseException {
        long n = -1;
        try{
            n = Long.parseLong(text);
        }catch(NumberFormatException ignored){ }
        Object output = null;
        if(n == -1) {
            output = text;
        }
        for(Class cls : domainClasses) {
            final Object update = converterFactory.getConverter(cls).convert(text);
            if( ! Objects.equals(update, text)) {
                output = update;
                break;
            }
        }
        LOG.trace("Converted: {} to: {}, using locale: {}", text, output, locale);
        return output == null ? text : output;
    }
}
