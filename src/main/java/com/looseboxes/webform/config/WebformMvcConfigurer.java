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
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
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
import com.bc.webform.functions.TypeTests;
import com.looseboxes.webform.WebformDefaults;
import com.looseboxes.webform.converters.DomainTypeConverter;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.StringToTemporalConverter;
import com.looseboxes.webform.converters.StringToTemporalConverterImpl;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.converters.TemporalToStringConverterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:26:12 AM
 */
@Configuration
public class WebformMvcConfigurer implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(WebformMvcConfigurer.class);
    
    @Autowired private EntityRepositoryFactory repoFactory;
    @Autowired private PropertySearch propertySearch;
    @Autowired private TypeTests typeTests;
    @Autowired private DomainClasses domainClasses;
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        
        log.debug("Adding formatters");

        registry.addConverter(this.stringEmptyToNullConverter());
        registry.addConverter(this.multipartFileToStringConverter());
        registry.addConverter(this.stringToDateConverter());
        registry.addConverter(this.dateToStringConverter());
        registry.addConverter(this.stringToTemporalConverter());
        registry.addConverter(this.temporalToStringConverter());
        registry.addConverter(this.domainTypeConverter());

        registry.addPrinter(this.domainObjectPrinter());
//        registry.addConverterFactory(this.idToDomainTypeConverterFactory());
    }
    
    @Bean public DomainTypeConverter domainTypeConverter() {
        final DomainTypeConverter genericConverter = new DomainTypeConverter(
                domainClasses.get(), 
                this.domainTypeToStringConverter(), 
                this.idToDomainTypeConverterFactory());
        return genericConverter;
    }
    
    @Bean public DomainTypeToStringConverter domainTypeToStringConverter() {
        return new DomainTypeToStringConverter(this.typeTests,
                this.domainObjectPrinter(),
                WebformDefaults.LOCALE
        );
    }
    
    @Bean public DomainObjectPrinter domainObjectPrinter() {
        return new DomainObjectPrinterImpl(
                this.propertySearch, this.repoFactory);
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