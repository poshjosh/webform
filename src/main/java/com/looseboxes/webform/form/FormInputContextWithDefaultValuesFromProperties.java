package com.looseboxes.webform.form;

import com.bc.reflection.ReflectionUtil;
import com.bc.webform.TypeTests;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import com.looseboxes.webform.util.TextExpressionResolver;

/**
 * @author hp
 */
public class FormInputContextWithDefaultValuesFromProperties extends FormInputContextImpl{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormInputContextWithDefaultValuesFromProperties.class);
    
    private final TypeTests typeTests;
    private final TextExpressionResolver propertyExpressionResolver;
    private final DateToStringConverter dateToStringConverter;
    private final TemporalToStringConverter temporalToStringConverter;
    private final DomainTypeToIdConverter domainTypeToIdConverter;

    public FormInputContextWithDefaultValuesFromProperties(
            TypeTests typeTests,
            PropertySearch propertySearch, 
            TextExpressionResolver propertyExpressionResolver,
            DateToStringConverter dateToStringConverter, 
            TemporalToStringConverter temporalToStringConverter,
            DomainTypeToIdConverter entityToIdConverter) {
        super(propertySearch);
        this.typeTests = Objects.requireNonNull(typeTests);
        this.propertyExpressionResolver = Objects.requireNonNull(propertyExpressionResolver);
        this.dateToStringConverter = Objects.requireNonNull(dateToStringConverter);
        this.temporalToStringConverter = Objects.requireNonNull(temporalToStringConverter);
        this.domainTypeToIdConverter = Objects.requireNonNull(entityToIdConverter);
    }
    
    @Override
    public Object getValue(Object source, Field field) {
        
        final Object fieldValue = super.getValue(source, field);
        
        Object result;
        
        if(fieldValue != null){
            result = fieldValue;
        }else{    
        
            final String propStr = this.getValueFromProperties(field, null);
            
            if(propStr == null) {
                
                result = null;
                
            }else if(propertyExpressionResolver.isExpression(propStr)) {
                
                result = propertyExpressionResolver.resolve(propStr, field.getType(), null);
                
            }else{
                
                result = propStr;
            }
        }

        final boolean resultNotFromFieldValue = fieldValue == null;
        
        if(result != null && resultNotFromFieldValue) {

            if(field.getType().isAssignableFrom(result.getClass())) {

                // This value is from the properties file
                // We now use it to update the model object i.e source or
                // declaring instance of the field
                // 
                if( ! this.setValue(source, field, result)) {

                    LOG.warn("Failed to set value to {}, for: {}.{}", result,
                            source.getClass().getSimpleName(), field.getName());
                }
            }else{

                LOG.warn("Type mismatch: field type: {} is not assignable from value type: {}",
                        field.getType(), result.getClass());
            }
        }
        
        result = this.format(source, field, result);
        
        LOG.trace("Value: {}, field: {}.{}", result, 
                field.getDeclaringClass().getName(), field.getName());
        
        return result;
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
            
            fieldValue = this.domainTypeToIdConverter.convert(fieldValue);
            
        }else if (fieldValue instanceof Collection) {
            
            final Collection currentValue = (Collection)fieldValue;
            
            if( ! currentValue.isEmpty()) {
                final Collection update = (Collection)new ReflectionUtil()
                        .newInstanceForCollectionType(fieldValue.getClass());
                fieldValue = currentValue.stream()
                        .map((e) -> domainTypeToIdConverter.convert(e))
                        .collect(Collectors.toCollection(() -> update));
                
                LOG.trace("For field: {}\nConverted: {}\n       To: {}", 
                        field, currentValue, fieldValue);
            }
        }
        
        return fieldValue;
    }
    
    public String getValueFromProperties(Field field, String resultIfNone) {
        
        final String valStr = this.getPropertySearch().findOrDefault(
                WebformProperties.FIELD_DEFAULT_VALUE, field, null);
        
        return valStr == null || valStr.isEmpty() ? resultIfNone : valStr;
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
