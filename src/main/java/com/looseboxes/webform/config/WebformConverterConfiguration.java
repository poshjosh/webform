package com.looseboxes.webform.config;

import com.looseboxes.webform.converters.DateAndTimePatternsSupplier;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainTypeConverter;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.DomainTypeToStringConverter;
import com.looseboxes.webform.converters.IdToDomainTypeConverterFactoryImpl;
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

/**
 * @author hp
 */
@Configuration
@ConditionalOnMissingBean(WebformConverterConfigurationSource.class)
public class WebformConverterConfiguration{
    
    private final WebformConverterConfigurationSource delegate;

    public WebformConverterConfiguration(ApplicationContext applicationContext) {
        delegate = new WebformConverterConfigurationSource(applicationContext);
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

    @Bean public DomainTypeToIdConverter entityToIdConverter() {
        return delegate.entityToIdConverter();
    }

    @Bean public IdToDomainTypeConverterFactoryImpl idToDomainTypeConverterFactory() {
        return delegate.idToDomainTypeConverterFactory();
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