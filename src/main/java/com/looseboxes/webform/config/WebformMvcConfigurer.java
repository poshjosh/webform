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

import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainTypeConverter;
import com.looseboxes.webform.converters.MultipartFileToStringConverter;
import com.looseboxes.webform.converters.StringEmptyToNullConverter;
import com.looseboxes.webform.converters.StringToDateConverter;
import com.looseboxes.webform.converters.TemporalConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.looseboxes.webform.web.WebstoreValidatingDataBinder;
import java.util.Date;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import com.looseboxes.webform.converters.DomainTypePrinter;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:26:12 AM
 */
@Configuration
public class WebformMvcConfigurer implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(WebformMvcConfigurer.class);
    
    @Autowired private ApplicationContext context;
    @Autowired private Validator validator;
    
    @Bean WebstoreValidatingDataBinder webValidator(@Autowired DomainTypeConverter domainTypeConverter) {
        return new WebstoreValidatingDataBinder(webformConversionService(domainTypeConverter), this.validator);
    }
    
//    @Bean 
    public ConversionService webformConversionService(@Autowired DomainTypeConverter domainTypeConverter) {
        final DefaultFormattingConversionService wcs = new DefaultFormattingConversionService(){
            @Override
            protected GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
                GenericConverter converter = super.getConverter(sourceType, targetType);
                if(domainTypeConverter.isConvertible(sourceType.getType(), targetType.getType())) {
                    converter = domainTypeConverter;
                }
                log.trace("Source: {}, target: {}, converter: {}", 
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
        
        registry.addConverter(context.getBean(StringEmptyToNullConverter.class));
        
        replaceConverter(registry, MultipartFile.class, String.class, context.getBean(MultipartFileToStringConverter.class));
        
        replaceConverter(registry, String.class, Date.class, context.getBean(StringToDateConverter.class));
        
        replaceConverter(registry, Date.class, String.class, context.getBean(DateToStringConverter.class));
        
        registry.addConverter(context.getBean(TemporalConverter.class));
        
        registry.addConverter(context.getBean(DomainTypeConverter.class));
        
        registry.addPrinter(context.getBean(DomainTypePrinter.class));
    }
    
    public <S, T> void replaceConverter(FormatterRegistry registry, 
            Class<S> sourceType, Class<T> targetType, 
            Converter<? super S, ? extends T> converter){
        registry.removeConvertible(sourceType, targetType);
        registry.addConverter(sourceType, targetType, converter);
        
    }
}
/**
 * 
    @Override
    public void addFormatters(FormatterRegistry registry) {
        
        log.debug("Adding formatters");
        
        final WebformConverterConfigurationSource config = context.getBean(WebformConverterConfigurationSource.class);
        
        registry.addConverter(config.stringEmptyToNullConverter());
        
        replaceConverter(registry, MultipartFile.class, String.class, config.multipartFileToStringConverter());
        
        replaceConverter(registry, String.class, Date.class, config.stringToDateConverter());
        
        replaceConverter(registry, Date.class, String.class, config.dateToStringConverter());
        
        registry.addConverter(config.temporalConverter());
        
        registry.addConverter(config.domainTypeConverter());
        
        registry.addPrinter(config.domainObjectPrinter());
    }
 * 
 */