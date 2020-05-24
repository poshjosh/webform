package com.looseboxes.webform.form;

import com.bc.reflection.ReflectionUtil;
import com.bc.webform.functions.FormInputContextForJpaEntity;
import com.bc.webform.functions.TypeTests;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.EntityToIdConverter;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public class FormInputContextImpl extends FormInputContextForJpaEntity{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormInputContextImpl.class);
    
    private final TypeTests typeTests;
    private final PropertySearch propertySearch;
    private final PropertyExpressionsResolver propertyExpressionsResolver;
    private final DateToStringConverter dateToStringConverter;
    private final TemporalToStringConverter temporalToStringConverter;
    private final EntityToIdConverter entityToIdConverter;

    public FormInputContextImpl(
            TypeTests typeTests,
            PropertySearch propertySearch, 
            PropertyExpressionsResolver propertyExpressionsResolver,
            DateToStringConverter dateToStringConverter, 
            TemporalToStringConverter temporalToStringConverter,
            EntityToIdConverter entityToIdConverter) {
        this.typeTests = Objects.requireNonNull(typeTests);
        this.propertySearch = Objects.requireNonNull(propertySearch);
        this.propertyExpressionsResolver = Objects.requireNonNull(propertyExpressionsResolver);
        this.dateToStringConverter = Objects.requireNonNull(dateToStringConverter);
        this.temporalToStringConverter = Objects.requireNonNull(temporalToStringConverter);
        this.entityToIdConverter = Objects.requireNonNull(entityToIdConverter);
    }
    
    @Override
    public Object getValue(Object source, Field field) {
        
        Object fieldValue = super.getValue(source, field);
        
        if(fieldValue == null){
        
            final String propStr = this.getValueFromProperties(field, null);
            
            if(propStr == null) {
                
                fieldValue = null;
                
            }else if(this.isExpression(propStr)) {
                
                fieldValue = this.getExpressionValue(propStr, null);
                
            }else{
                
                fieldValue = propStr;
            }
            
            if(fieldValue != null) {
                
                if(field.getType().isAssignableFrom(fieldValue.getClass())) {
                
                    // This value is from the properties file
                    // We now use it to update the model object i.e source or
                    // declaring instance of the field
                    // 
                    if( ! this.setValue(source, field, fieldValue)) {

                        LOG.warn("Failed to set value to {}, for: {}.{}", fieldValue,
                                source.getClass().getSimpleName(), field.getName());
                    }
                }else{

                    LOG.warn("Type mismatch: field type: {} is not assignable from value type: {}",
                            field.getType(), fieldValue.getClass());
                }
            }
        }
        
        fieldValue = this.format(source, field, fieldValue);
        
        LOG.trace("Value: {}, field: {}.{}", fieldValue, 
                field.getDeclaringClass().getName(), field.getName());
        
        return fieldValue;
    }
    
    public Object format(Object source, Field field, Object fieldValue) {
        
        final javax.persistence.Temporal temporal;
        
        if(fieldValue instanceof Date && (temporal = field.getAnnotation(javax.persistence.Temporal.class)) != null) {
            
            final Date date = (Date)fieldValue;
            
            fieldValue = this.getDateToStringConverter(temporal).convert(date);
            
        }else if(fieldValue instanceof java.time.temporal.Temporal) {  
            
            final java.time.temporal.Temporal t = (java.time.temporal.Temporal)fieldValue;
           
            fieldValue = this.getTemporalToStringConverter(t).convert(t);
            
        }else if(this.getTypeTests().isDomainType(field.getType())) {
            
            fieldValue = this.entityToIdConverter.convert(fieldValue);
            
        }else if (fieldValue instanceof Collection) {
            
            final Collection currentValue = (Collection)fieldValue;
            
            if( ! currentValue.isEmpty()) {
                final Collection update = (Collection)new ReflectionUtil()
                        .newInstanceForCollectionType(fieldValue.getClass());
                fieldValue = currentValue.stream()
                        .map((e) -> entityToIdConverter.convert(e))
                        .collect(Collectors.toCollection(() -> update));
                
                LOG.trace("For field: {}\nConverted: {}\n       To: {}", 
                        field, currentValue, fieldValue);
            }
        }
        
        return fieldValue;
    }
    
    public String getValueFromProperties(Field field, String resultIfNone) {
        
        final String valStr = this.propertySearch.findOrDefault(
                WebformProperties.FIELD_DEFAULT_VALUE, field, null);
        
        return valStr == null || valStr.isEmpty() ? resultIfNone : valStr;
    }
    
    public Object getExpressionValue(String text, Object resultIfNone) {
        if( ! this.isExpression(text)) {
            throw new IllegalArgumentException(text);
        }    
        Object value = null;
        final String method = text.substring(1);
        switch(method) {
            case "current_date":
                value = propertyExpressionsResolver.current_date(); break;
            case "current_time":
                value = propertyExpressionsResolver.current_time(); break;
            case "current_timestamp":
                value = propertyExpressionsResolver.current_timestamp(); break;
            default:
                final Class type = this.propertyExpressionsResolver.getClass();
                try{
                    value = type.getMethod(method).invoke(this.propertyExpressionsResolver);
                }catch(IllegalAccessException | IllegalArgumentException | 
                        NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    LOG.warn("Could not resolve: " + text + " to any method in " + type, e);
                }
        }
        return value == null ? resultIfNone : value;
    }
    
    public boolean isExpression(String text) {
        return text.startsWith("#");
    }

    public Converter<Date, String> getDateToStringConverter(javax.persistence.Temporal temporal) {
        Converter<Date, String> selected;
        final javax.persistence.TemporalType temporalType = temporal.value();
        if(temporalType != null) {
            switch(temporalType) {
                case DATE:
                    selected = dateToStringConverter.dateInstance(); break;
                case TIME:
                    selected = dateToStringConverter.timeInstance(); break;
                case TIMESTAMP:
                    selected = dateToStringConverter.datetTimeInstance(); break;
                default:
                    throw Errors.unexpected(
                            temporalType, (Object[])javax.persistence.TemporalType.values());
            }
        }else{
            LOG.warn("Unable to deduce temporal type of @Temporal annotation. Date will be converted via toString() method. Temporal: {}", temporal);
            selected = (date) -> date.toString();
        }    
        return selected;
    }

    public Converter<java.time.temporal.Temporal, String> 
        getTemporalToStringConverter(java.time.temporal.Temporal temporal) {
        return this.temporalToStringConverter;
    }

    @Override
    public String getType(Object source, Field field) {

        String type = this.getTypeFromProperties(field, null);
        
        if(type == null) {
            
            type = super.getType(source, field);
        }
        
        LOG.trace("Type: {}, field: {}.{}", type, 
                field.getDeclaringClass().getName(), field.getName());
        
        return type;
    }    
    
    /**
     * @param field
     * @param defaultValue
     * @return defaultValue if none, otherwise return either: text, number, 
     * password, file, checkbox, radio, datetime-local, date, time, hidden
     */
    public String getTypeFromProperties(Field field, String defaultValue) {
        final String type = this.propertySearch
                .findOrDefault(WebformProperties.FIELD_TYPE, field, null);
        LOG.trace("Field type from properties: {}, field: {}", type, field);
        return type == null ? defaultValue : type;
    }    
    
    public boolean matches(String name, Field field) {
        if(name.equalsIgnoreCase(field.getName())) {
            return true;
        }
        final Column column = field.getAnnotation(Column.class);
        if(column != null && name.equalsIgnoreCase(column.name())) {
            return true;
        }
        return false;
    }
    
    public TypeTests getTypeTests() {
        return this.typeTests;
    }
}
