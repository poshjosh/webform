package com.looseboxes.webform;

import com.bc.jpa.spring.DomainClasses;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.functions.TypeTests;
import com.looseboxes.webform.converters.DateAndTimePatternsSupplier;
import com.looseboxes.webform.converters.DateAndTimePatternsSupplierImpl;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DateToStringConverterImpl;
import com.looseboxes.webform.converters.DomainObjectParser;
import com.looseboxes.webform.converters.DomainObjectParserImpl;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.converters.DomainObjectPrinterImpl;
import com.looseboxes.webform.converters.EntityToIdConverter;
import com.looseboxes.webform.converters.EntityToStringConverter;
import com.looseboxes.webform.converters.IdToEntityConverterFactory;
import com.looseboxes.webform.converters.MultipartFileToStringConverter;
import com.looseboxes.webform.converters.StringEmptyToNullConverter;
import com.looseboxes.webform.converters.StringToDateConverter;
import com.looseboxes.webform.util.PropertySearch;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author hp
 */
public class WebformConverters {
    
    private static final Logger LOG = LoggerFactory.getLogger(WebformConverters.class);
    
    @Autowired private EntityRepositoryFactory repoFactory;
    @Autowired private DomainClasses domainClassesSupplier;
    @Autowired private PropertySearch propertySearch;
    @Autowired private TypeTests typeTests;

    @Bean public EntityToIdConverter entityToIdConverter() {
        return new EntityToIdConverter(this.repoFactory);
    }
    
    @Bean public EntityToStringConverter entityToStringConverter() {
        return new EntityToStringConverter(this.typeTests,
                this.domainObjectPrinter(),
                WebformDefaults.LOCALE
        );
    }
    
    @Bean public DomainObjectPrinter domainObjectPrinter() {
        return new DomainObjectPrinterImpl(
                this.propertySearch, this.repoFactory);
    }
    
    @Bean public DomainObjectParser domainObjectParser() {
        
        final Set<Class> classes = this.domainClassesSupplier.get();
        
        return new DomainObjectParserImpl(
                this.idToEntityConverterFactory(), 
                classes.toArray(new Class[classes.size()]));
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
    
    @Bean public IdToEntityConverterFactory idToEntityConverterFactory() {
        return new IdToEntityConverterFactory(this.repoFactory);
    }
    
    @Bean public DateAndTimePatternsSupplier dateAndTimePatternsSupplier() {
        return new DateAndTimePatternsSupplierImpl(this.propertySearch);
    }
}
