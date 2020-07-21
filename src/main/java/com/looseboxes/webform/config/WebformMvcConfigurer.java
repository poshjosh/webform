/*
 * Copyright 2019 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.looseboxes.webform.config;

import com.bc.jpa.spring.DomainClasses;
import com.looseboxes.webform.util.PropertySearch;
import com.looseboxes.webform.converters.DateAndTimePatternsSupplier;
import com.looseboxes.webform.converters.DateAndTimePatternsSupplierImpl;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DateToStringConverterImpl;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.converters.IdToDomainTypeConverterFactoryImpl;
import com.looseboxes.webform.converters.MultipartFileToStringConverter;
import com.looseboxes.webform.converters.StringEmptyToNullConverter;
import com.looseboxes.webform.converters.StringToDateConverter;
import com.looseboxes.webform.converters.DomainObjectPrinterImpl;
import com.looseboxes.webform.converters.DomainTypeToStringConverter;
import com.bc.webform.TypeTests;
import com.looseboxes.webform.WebformDefaults;
import com.looseboxes.webform.converters.DomainTypeConverter;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.StringToTemporalConverter;
import com.looseboxes.webform.converters.StringToTemporalConverterImpl;
import com.looseboxes.webform.converters.TemporalConverter;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.converters.TemporalToStringConverterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.web.WebValidator;
import java.util.Date;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:26:12 AM
 */
@Configuration
public class WebformMvcConfigurer implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(WebformMvcConfigurer.class);
    
    @Autowired private EntityRepositoryProvider repoFactory;
    @Autowired private PropertySearch propertySearch;
    @Autowired private TypeTests typeTests;
    @Autowired private DomainClasses domainClasses;
    @Autowired private Validator validator;
    
    @Bean WebValidator webValidator() {
        return new WebValidator(this.webformConversionService(), this.validator);
    }
    
//    @Bean 
        ConversionService webformConversionService() {
        final DefaultFormattingConversionService wcs = new DefaultFormattingConversionService(){
            @Override
            protected GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
                GenericConverter converter = super.getConverter(sourceType, targetType);
                log.debug("Source: {}, target: {}, converter: {}", 
                        sourceType.getName(), targetType.getName(), converter);
                return converter;
            }
        
        };
        
        this.addFormatters(wcs);
        return wcs;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        
        log.debug("Adding formatters");
        
        registry.addConverter(this.stringEmptyToNullConverter());
        
        replaceConverter(registry, MultipartFile.class, String.class, multipartFileToStringConverter());
        
        replaceConverter(registry, String.class, Date.class, stringToDateConverter());
        
        replaceConverter(registry, Date.class, String.class, dateToStringConverter());
        
        registry.addConverter(this.temporalConverter());
        
        registry.addConverter(this.domainTypeConverter());
        
        registry.addPrinter(this.domainObjectPrinter());
    }
    
    public <S, T> void replaceConverter(FormatterRegistry registry, 
            Class<S> sourceType, Class<T> targetType, 
            Converter<? super S, ? extends T> converter){
        registry.removeConvertible(sourceType, targetType);
        registry.addConverter(sourceType, targetType, converter);
        
    }
    
    @Bean public DomainTypeConverter domainTypeConverter() {
        final DomainTypeConverter genericConverter = new DomainTypeConverter(
                domainClasses.get(), 
                this.domainTypeToStringConverter(), 
                this.idToDomainTypeConverterFactory());
        return genericConverter;
    }
    
    @Bean public TemporalConverter temporalConverter() {
        return new TemporalConverter(
                this.temporalToStringConverter(), this.stringToTemporalConverter());
    }
    
    @Bean public DomainTypeToStringConverter domainTypeToStringConverter() {
        return new DomainTypeToStringConverter(this.typeTests,
                this.domainObjectPrinter(),
                WebformDefaults.LOCALE
        );
    }
    
    @Bean public DomainObjectPrinter domainObjectPrinter() {
        return new DomainObjectPrinterImpl(this.propertySearch);
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
        return new StringToTemporalConverterImpl(this.dateAndTimePatternsSupplier());
    }
    
    @Bean public TemporalToStringConverter temporalToStringConverter() {
        return new TemporalToStringConverterImpl(this.dateAndTimePatternsSupplier());
    }

    @Bean public IdToDomainTypeConverterFactoryImpl idToDomainTypeConverterFactory() {
        return new IdToDomainTypeConverterFactoryImpl(this.repoFactory);
    }
    
    @Bean public DateAndTimePatternsSupplier dateAndTimePatternsSupplier() {
        return new DateAndTimePatternsSupplierImpl(this.propertySearch);
    }

    @Bean public DomainTypeToIdConverter entityToIdConverter() {
        return new DomainTypeToIdConverter(this.repoFactory);
    }
}
/**
 * 
 * 
        StringToTemporalConverter stringToTemporalConverter = this.stringToTemporalConverter();
        replaceConverter(registry, String.class, Instant.class, stringToTemporalConverter.instantInstance());
        replaceConverter(registry, String.class, LocalDate.class, stringToTemporalConverter.localDateInstance());
        replaceConverter(registry, String.class, LocalDateTime.class, stringToTemporalConverter.localDateTimeInstance());
        replaceConverter(registry, String.class, LocalTime.class, stringToTemporalConverter.localTimeInstance());
        replaceConverter(registry, String.class, ZonedDateTime.class, stringToTemporalConverter.zonedDateTimeInstance());
        
        TemporalToStringConverter temporalToStringConverter = this.temporalToStringConverter();
        replaceConverter(registry, Instant.class, String.class, temporalToStringConverter);
        replaceConverter(registry, LocalDate.class, String.class, temporalToStringConverter);
        replaceConverter(registry, LocalDateTime.class, String.class, temporalToStringConverter);
        replaceConverter(registry, LocalTime.class, String.class, temporalToStringConverter);
        replaceConverter(registry, ZonedDateTime.class, String.class, temporalToStringConverter);
        
 * 
 */