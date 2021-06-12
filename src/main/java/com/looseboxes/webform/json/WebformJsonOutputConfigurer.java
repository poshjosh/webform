package com.looseboxes.webform.json;

import com.bc.webform.form.Form;
import com.bc.webform.form.member.FormMember;
import com.bc.webform.TypeTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author hp
 */
@Component
public class WebformJsonOutputConfigurer {
    
    private final TypeTests typeTests;

    @Autowired
    public WebformJsonOutputConfigurer(TypeTests typeTests) {
        this.typeTests = Objects.requireNonNull(typeTests);
    }

    public void configureHttpMessageConverter(
            List<HttpMessageConverter<?>> converters) {
        
        this.configureHttpMessageConverter(converters, this.createObjectMapper());
    }
    
    public void configureHttpMessageConverter(
            List<HttpMessageConverter<?>> converters, ObjectMapper mapper) {
    
        final MappingJackson2HttpMessageConverter converter = 
                this.createConfiguredHttpMessageConverter(mapper);

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

    public RestTemplate createConfiguredRestTemplate() {
        return this.createConfiguredRestTemplate(this.createObjectMapper());
    }
    
    public RestTemplate createConfiguredRestTemplate(ObjectMapper objectMapper) {

        final RestTemplate restTemplate = new RestTemplate();

        // Add it to the beginning of the list so that it takes precedence 
        // over any default that Spring has registered
        restTemplate.getMessageConverters().add(0, 
                this.createConfiguredHttpMessageConverter(objectMapper));

        return restTemplate;
    }

    public MappingJackson2HttpMessageConverter createConfiguredHttpMessageConverter() {
        return this.createConfiguredHttpMessageConverter(this.createObjectMapper());
    }
    
    public MappingJackson2HttpMessageConverter createConfiguredHttpMessageConverter(
            ObjectMapper objectMapper) {
        
        objectMapper = this.configure(objectMapper);

        final MappingJackson2HttpMessageConverter converter = 
                new MappingJackson2HttpMessageConverter(objectMapper);
        
        return converter;
    }
    
    public ObjectMapper createConfiguredObjectMapper() {
        return this.configure(this.createObjectMapper());
    }
    
    private ObjectMapper createObjectMapper() {
        return Jackson2ObjectMapperBuilder.json().build();
    }
    
    public ObjectMapper configure(ObjectMapper objectMapper) {
        return this.configure(objectMapper, new SimpleFilterProvider());
    }
    
    public ObjectMapper configure(
            ObjectMapper objectMapper, SimpleFilterProvider filters) {
        this.addFilters(filters);
        this.addMixIns(objectMapper);
        objectMapper.setFilterProvider(filters);
        return objectMapper;
    }
    
    public ObjectMapper addMixIns(ObjectMapper objectMapper) {
        return objectMapper
                .addMixIn(Form.class, JacksonFormMixIn.class)
                .addMixIn(FormMember.class, JacksonFormMemberMixIn.class);
    }
    
    public SimpleFilterProvider addFilters(SimpleFilterProvider filters) {
        filters.addFilter(JacksonFormMemberFilter.FILTER_ID, jacksonFormMemberFilter())
                .addFilter(JacksonFormFilter.FILTER_ID, jacksonFormFilter());
        return filters;
    }

    public JacksonFormMemberFilter jacksonFormMemberFilter() {
        return new JacksonFormMemberFilter(this.typeTests);
    }
    
    public JacksonFormFilter jacksonFormFilter() {
        return new JacksonFormFilter(this.typeTests);
    }
}
