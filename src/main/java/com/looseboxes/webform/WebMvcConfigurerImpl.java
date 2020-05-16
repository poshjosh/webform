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

package com.looseboxes.webform;

import com.bc.jpa.spring.DomainClasses;
import com.looseboxes.webform.store.PropertySearch;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.looseboxes.webform.converters.DateAndTimePatternsSupplier;
import com.looseboxes.webform.converters.DateAndTimePatternsSupplierImpl;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DateToStringConverterImpl;
import com.looseboxes.webform.converters.DomainObjectParser;
import com.looseboxes.webform.converters.DomainObjectParserImpl;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.converters.IdToEntityConverterFactory;
import com.looseboxes.webform.converters.MultipartFileToStringConverter;
import com.looseboxes.webform.converters.StringEmptyToNullConverter;
import com.looseboxes.webform.converters.StringToDateConverter;
import com.looseboxes.webform.converters.DomainObjectPrinterImpl;
import com.looseboxes.webform.converters.EntityToIdConverter;
import com.looseboxes.webform.converters.EntityToStringConverter;
//import com.looseboxes.webform.converters.StringIdToBlogTypeConverter;
import com.bc.webform.functions.TypeTests;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:26:12 AM
 */
public class WebMvcConfigurerImpl implements WebMvcConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(WebMvcConfigurerImpl.class);
    
    @Autowired private EntityRepositoryFactory repoFactory;
    @Autowired private DomainClasses domainClassesSupplier;
    @Autowired private PropertySearch propertySearch;
    @Autowired private TypeTests typeTests;
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        
        LOG.debug("Adding formatters");

        registry.addConverter(this.stringEmptyToNullConverter());
        registry.addConverter(this.multipartFileToStringConverter());
        registry.addConverter(this.stringToDateConverter());
        registry.addConverter(this.dateToStringConverter());
        registry.addConverterFactory(this.idToEntityConverterFactory());
        registry.addConverter(this.entityToStringConverter());

        registry.addPrinter(this.domainObjectPrinter());
        registry.addParser(this.domainObjectParser());
    }
    
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