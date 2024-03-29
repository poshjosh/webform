package com.looseboxes.webform.config;

import com.bc.jpa.spring.DomainClasses;
import com.bc.webform.TypeTests;
import com.looseboxes.webform.WebformLocaleSupplier;
import com.looseboxes.webform.converters.DateAndTimePatternsSupplier;
import com.looseboxes.webform.converters.DateAndTimePatternsSupplierImpl;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DateToStringConverterImpl;
import com.looseboxes.webform.converters.DomainTypePrinterImpl;
import com.looseboxes.webform.converters.DomainTypeConverter;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.DomainTypeToStringConverter;
import com.looseboxes.webform.converters.IdStringToDomainTypeConverterFactoryImpl;
import com.looseboxes.webform.converters.MultipartFileToStringConverter;
import com.looseboxes.webform.converters.StringEmptyToNullConverter;
import com.looseboxes.webform.converters.StringToDateConverter;
import com.looseboxes.webform.converters.StringToTemporalConverter;
import com.looseboxes.webform.converters.StringToTemporalConverterImpl;
import com.looseboxes.webform.converters.TemporalConverter;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.converters.TemporalToStringConverterImpl;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.util.PropertySearch;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import org.springframework.context.ApplicationContext;
import com.looseboxes.webform.converters.DomainTypePrinter;
import com.looseboxes.webform.converters.EntityToSelectOptionConverter;
import com.looseboxes.webform.converters.EntityToSelectOptionConverterImpl;
import org.springframework.context.annotation.Bean;
import com.looseboxes.webform.converters.IdStringToDomainTypeConverterFactory;

/**
 * @author hp
 */
public class WebformConverterConfigurationSource{
    
    private final ApplicationContext applicationContext;

    public WebformConverterConfigurationSource(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext);
    }
    
    @Bean public EntityToSelectOptionConverter entityToSelectOptionConverter() {
        return new EntityToSelectOptionConverterImpl(
                this.domainTypeToIdConverter(), this.domainTypePrinter());
    }
    
    @Bean public DomainTypeConverter domainTypeConverter() {
        final DomainTypeConverter genericConverter = new DomainTypeConverter(
                this.getDomainClasses().get(), 
                this.domainTypeToStringConverter(), 
                this.idStringToDomainTypeConverterFactory());
        return genericConverter;
    }
    
    @Bean public TemporalConverter temporalConverter() {
        return new TemporalConverter(
                this.temporalToStringConverter(), this.stringToTemporalConverter());
    }
    
    @Bean public DomainTypeToStringConverter domainTypeToStringConverter() {
        return new DomainTypeToStringConverter(this.getTypeTests(),
                this.domainTypePrinter(),
                WebformLocaleSupplier.getLocale(applicationContext)
        );
    }
    
    @Bean public DomainTypePrinter domainTypePrinter() {
        return new DomainTypePrinterImpl(this.getPropertySearch());
    }
    
    @Bean public StringEmptyToNullConverter stringEmptyToNullConverter() {
        return new StringEmptyToNullConverter();
    }
    
    @Bean public MultipartFileToStringConverter multipartFileToStringConverter() {
        return new MultipartFileToStringConverter();
    }
    
    @Bean public StringToDateConverter stringToDateConverter() {
        return new StringToDateConverter(this.dateAndTimePatternsSupplier());
    }
    
    @Bean public DateToStringConverter dateToStringConverter() {
        return new DateToStringConverterImpl(this.dateAndTimePatternsSupplier());
    }
    
    @Bean public StringToTemporalConverter stringToTemporalConverter() {
        return new StringToTemporalConverterImpl(this.dateAndTimePatternsSupplier(), getZoneId());
    }

    @Bean public TemporalToStringConverter temporalToStringConverter() {
        return new TemporalToStringConverterImpl(this.dateAndTimePatternsSupplier(), getZoneId());
    }

    public ZoneId getZoneId() {
        return ZoneOffset.UTC;
    }

    @Bean public IdStringToDomainTypeConverterFactory idStringToDomainTypeConverterFactory() {
        return new IdStringToDomainTypeConverterFactoryImpl(this.getEntityRepositoryProvider());
    }
    
    @Bean public DateAndTimePatternsSupplier dateAndTimePatternsSupplier() {
        return new DateAndTimePatternsSupplierImpl(this.getPropertySearch());
    }

    @Bean public DomainTypeToIdConverter domainTypeToIdConverter() {
        return new DomainTypeToIdConverter(this.getEntityRepositoryProvider());
    }

    private EntityRepositoryProvider getEntityRepositoryProvider() {
        return applicationContext.getBean(EntityRepositoryProvider.class);
    }

    private PropertySearch getPropertySearch() {
        return applicationContext.getBean(PropertySearch.class);
    }

    private TypeTests getTypeTests() {
        return applicationContext.getBean(TypeTests.class);
    }

    private DomainClasses getDomainClasses() {
        return applicationContext.getBean(DomainClasses.class);
    }
}
