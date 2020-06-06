package com.looseboxes.webform.form;

import com.bc.webform.functions.TypeTests;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class JacksonDomainTypeFilter extends SimpleBeanPropertyFilter{
    
    private static final Logger LOG = LoggerFactory.getLogger(JacksonDomainTypeFilter.class);
    
    public static final String FILTER_ID = "JacksonDomainTypeFilter";
    
    private final TypeTests typeTests;

    public JacksonDomainTypeFilter(TypeTests typeTests) { 
        this.typeTests = Objects.requireNonNull(typeTests);
    }
    
    @Override
    public void serializeAsElement(Object elementValue, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        final boolean ignore = elementValue != null && ignore(
                writer.getMember().getDeclaringClass(), writer.getName(), elementValue);
        if( ! ignore) {
            
            super.serializeAsElement(elementValue, jgen, provider, writer); 
        }
    }

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        final Object value = getValue(pojo, writer);
        final boolean ignore = value != null && ignore(pojo.getClass(), writer.getName(), value);
        if( ! ignore) {
            super.serializeAsField(pojo, jgen, provider, writer); 
        }
    }
    
    public Object getValue(Object pojo, PropertyWriter writer) {
        try{
            final AnnotatedMember member = writer.getMember();
            if(member != null) {
                final Object value = member.getValue(pojo);
                return value;
            }
        }catch(RuntimeException e) {
            LOG.warn("", e);
        }
        return null;
    }

    public boolean ignore(Class parentType, String name, Object value) {
        
        final boolean ignore = this.isDomainType(value); 

        LOG.trace("Ignore: {}, parent type: {}, {} = {}", ignore,
                parentType.getSimpleName(), name, value);

        return ignore;
    }
    
    public boolean isDomainType(Object instance) {
        return instance == null ? false : typeTests.isDomainType(instance.getClass());
    }
}
