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
import com.looseboxes.webform.util.PropertySearch;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.Form;
import com.bc.webform.FormMember;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.looseboxes.webform.converters.ConverterImpl;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.StringToTemporalConverter;
import com.looseboxes.webform.converters.StringToTemporalConverterImpl;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.converters.TemporalToStringConverterImpl;
import com.looseboxes.webform.form.JacksonDomainTypeFilter;
import com.looseboxes.webform.form.JacksonFormMemberFilter;
import com.looseboxes.webform.form.JacksonFormMemberMixIn;
import com.looseboxes.webform.form.JacksonFormMixIn;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:26:12 AM
 */
@Configuration
public class WebMvcConfigurerImpl implements WebMvcConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(WebMvcConfigurerImpl.class);
    
    @Autowired private EntityRepositoryFactory repoFactory;
    @Autowired private PropertySearch propertySearch;
    @Autowired private TypeTests typeTests;
    @Autowired private DomainClasses domainClasses;
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        
        LOG.debug("Adding formatters");

        registry.addConverter(this.stringEmptyToNullConverter());
        registry.addConverter(this.multipartFileToStringConverter());
        registry.addConverter(this.stringToDateConverter());
        registry.addConverter(this.dateToStringConverter());
        registry.addConverter(this.stringToTemporalConverter());
        registry.addConverter(this.temporalToStringConverter());
        registry.addConverter(this.genericConverter());

        registry.addPrinter(this.domainObjectPrinter());
    }
    
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        final MappingJackson2HttpMessageConverter converter = 
                this.mappingJacksonHttpMessageConverter();

        this.replaceJackson2HttpMessageConverter(converters, converter);
    }
    
    public void replaceJackson2HttpMessageConverter(
            List<HttpMessageConverter<?>> converters,
            MappingJackson2HttpMessageConverter converter) {
    
        int indexToReplace = -1;
        for(int i=0; i<converters.size(); i++) {
            final HttpMessageConverter existing = converters.get(i);
            if(existing instanceof MappingJackson2HttpMessageConverter) {
                indexToReplace = i;
                break;
            }
        }
        
        if(indexToReplace != -1) {
            converters.set(indexToReplace, converter);
        }else{
            converters.add(converter);
        }
    }
    
    @Bean public RestTemplate restTemplate() {

        final RestTemplate restTemplate = new RestTemplate();

        // Add it to the beginning of the list so that it takes precedence 
        // over any default that Spring has registered
        restTemplate.getMessageConverters().add(0, mappingJacksonHttpMessageConverter());

        return restTemplate;
    }
    
    @Bean public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {

        final MappingJackson2HttpMessageConverter converter = 
                new MappingJackson2HttpMessageConverter(this.objectMapper());
        
        return converter;
    }
    
    @Bean public ObjectMapper objectMapper() {

        final FilterProvider filters = new SimpleFilterProvider()
                .addFilter(JacksonFormMemberFilter.FILTER_ID, 
                        this.jacksonFormMemberFilter())
                .addFilter(JacksonDomainTypeFilter.FILTER_ID, 
                        this.jacksonDomainTypeFilter());
        
        final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
                .json()
                .filters(filters)
                .mixIn(Form.class, JacksonFormMixIn.class)
                .mixIn(FormMember.class, JacksonFormMemberMixIn.class).build();
         
        return objectMapper;
    }
    
    @Bean public JacksonFormMemberFilter jacksonFormMemberFilter() {
        return new JacksonFormMemberFilter(this.typeTests);
    }
    
    @Bean public JacksonDomainTypeFilter jacksonDomainTypeFilter() {
        return new JacksonDomainTypeFilter(this.typeTests);
    }

    @Bean public GenericConverter genericConverter() {
        final GenericConverter genericConverter = new ConverterImpl(
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
        return new IdToDomainTypeConverterFactoryImpl(
                this.repoFactory, this.propertySearch);
    }
    
    @Bean public DateAndTimePatternsSupplier dateAndTimePatternsSupplier() {
        return new DateAndTimePatternsSupplierImpl(this.propertySearch);
    }

    @Bean public DomainTypeToIdConverter entityToIdConverter() {
        return new DomainTypeToIdConverter(this.repoFactory);
    }
}