package com.looseboxes.webform.config;

import com.looseboxes.webform.converters.DateAndTimePatternsSupplier;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainTypeConverter;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.DomainTypeToStringConverter;
import com.looseboxes.webform.converters.MultipartFileToStringConverter;
import com.looseboxes.webform.converters.StringEmptyToNullConverter;
import com.looseboxes.webform.converters.StringToDateConverter;
import com.looseboxes.webform.converters.StringToTemporalConverter;
import com.looseboxes.webform.converters.TemporalConverter;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.looseboxes.webform.converters.DomainTypePrinter;
import com.looseboxes.webform.converters.EntityToSelectOptionConverter;
import com.looseboxes.webform.converters.IdStringToDomainTypeConverterFactory;

/**
 * @author hp
 */
@Configuration
@ConditionalOnMissingBean(WebformConverterConfigurationSource.class)
public class WebformDefaultConverterConfiguration{
    
    private final WebformConverterConfigurationSource delegate;

    public WebformDefaultConverterConfiguration(ApplicationContext applicationContext) {
        delegate = new WebformConverterConfigurationSource(applicationContext);
    }

    @Bean public EntityToSelectOptionConverter entityToSelectOptionConverter() {
        return delegate.entityToSelectOptionConverter();
    }
    
    @Bean public DateAndTimePatternsSupplier dateAndTimePatternsSupplier() {
        return delegate.dateAndTimePatternsSupplier();
    }

    @Bean public DateToStringConverter dateToStringConverter() {
        return delegate.dateToStringConverter();
    }

    @Bean public DomainTypePrinter domainTypePrinter() {
        return delegate.domainTypePrinter();
    }

    @Bean public DomainTypeConverter domainTypeConverter() {
        return delegate.domainTypeConverter();
    }

    @Bean public DomainTypeToStringConverter domainTypeToStringConverter() {
        return delegate.domainTypeToStringConverter();
    }

    @Bean public DomainTypeToIdConverter domainTypeToIdConverter() {
        return delegate.domainTypeToIdConverter();
    }

    @Bean public IdStringToDomainTypeConverterFactory idToDomainTypeConverterFactory() {
        return delegate.idStringToDomainTypeConverterFactory();
    }

    @Bean public MultipartFileToStringConverter multipartFileToStringConverter() {
        return delegate.multipartFileToStringConverter();
    }

    @Bean public StringEmptyToNullConverter stringEmptyToNullConverter() {
        return delegate.stringEmptyToNullConverter();
    }

    @Bean public StringToDateConverter stringToDateConverter() {
        return delegate.stringToDateConverter();
    }

    @Bean public StringToTemporalConverter stringToTemporalConverter() {
        return delegate.stringToTemporalConverter();
    }

    @Bean public TemporalConverter temporalConverter() {
        return delegate.temporalConverter();
    }

    @Bean public TemporalToStringConverter temporalToStringConverter() {
        return delegate.temporalToStringConverter();
    }
}
