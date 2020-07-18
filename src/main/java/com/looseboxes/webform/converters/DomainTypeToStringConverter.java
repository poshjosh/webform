package com.looseboxes.webform.converters;

import com.bc.webform.TypeTests;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public class DomainTypeToStringConverter implements Converter<Object, String> {

    private static final Logger LOG = LoggerFactory.getLogger(DomainTypeToStringConverter.class);
    
    private final TypeTests typeTests;
    private final DomainObjectPrinter printer;
    private final Locale locale;

    public DomainTypeToStringConverter(TypeTests typeTests, 
            DomainObjectPrinter printer, Locale locale) {
        this.typeTests = Objects.requireNonNull(typeTests);
        this.printer = Objects.requireNonNull(printer);
        this.locale = Objects.requireNonNull(locale);
    }
    
    @Override
    public String convert(Object source) {
        final String output;
        if(source == null) {
            output = "";
        }else{
            if(this.typeTests.isDomainType(source.getClass())) {
                output = this.printer.print(source, locale);
            }else{
                output = source.toString();
            }
        }
        LOG.trace("Converted: {} to: {}", source , output);
        return output;
    }
    
}
