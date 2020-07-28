package com.looseboxes.webform.form;

import com.bc.reflection.ReflectionUtil;
import com.bc.webform.TypeTests;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainTypeConverter;
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
import java.util.Locale;
import org.springframework.core.convert.TypeDescriptor;
import com.looseboxes.webform.converters.DomainTypePrinter;

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
    private final DomainTypeConverter domainTypeConverter;
    private final EntityToSelectOptionConverter entityToSelectOptionConverter;

    public FormInputContextWithDefaultValuesFromProperties(
            TypeTests typeTests,
            PropertySearch propertySearch, 
            TextExpressionResolver propertyExpressionResolver,
            DateToStringConverter dateToStringConverter, 
            TemporalToStringConverter temporalToStringConverter,
            DomainTypeToIdConverter entityToIdConverter,
            DomainTypeConverter domainTypeConverter,
            DomainTypePrinter domainObjectPrinter,
            EntityToSelectOptionConverter entityToSelectOptionConverter) {
        super(propertySearch);
        this.typeTests = Objects.requireNonNull(typeTests);
        this.propertyExpressionResolver = Objects.requireNonNull(propertyExpressionResolver);
        this.dateToStringConverter = Objects.requireNonNull(dateToStringConverter);
        this.temporalToStringConverter = Objects.requireNonNull(temporalToStringConverter);
        this.domainTypeToIdConverter = Objects.requireNonNull(entityToIdConverter);
        this.domainTypeConverter = Objects.requireNonNull(domainTypeConverter);
        this.entityToSelectOptionConverter = Objects.requireNonNull(entityToSelectOptionConverter);
    }
    
    @Override
    public Object getValue(Object source, Field field) {
        
        final Object fieldValue = super.getValue(source, field);
        
        LOG.trace("{}.{} = {}", field.getDeclaringClass().getSimpleName(), 
                field.getName(), fieldValue);
        
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

            boolean assign = false;
            
            if(field.getType().isAssignableFrom(result.getClass())) {
                assign = true;
            }else{
                
                final Class srcType = result.getClass();
                final Class tgtType = field.getType();
                
                if(domainTypeConverter.isConvertible(srcType, tgtType)) {
                    result = domainTypeConverter.convert(result, TypeDescriptor.valueOf(srcType), TypeDescriptor.valueOf(tgtType));
                    assign = true;
                }else{
                    LOG.warn("Type mismatch. {} of type: {} is not assignable to type: {}",
                            result, srcType, tgtType);
                }
            }
            if(assign) {
                // This value is from the properties file
                // We now use it to update the model object i.e source or
                // declaring instance of the field
                // 
                if( ! this.setValue(source, field, result)) {

                    LOG.warn("Failed to set value to {}, for: {}.{}", result,
                            source.getClass().getSimpleName(), field.getName());
                }
            }
        }
        
        result = this.format(source, field, result);
        
        LOG.trace("Converted {} to {} for {}.{}", fieldValue, result, 
                field.getDeclaringClass().getSimpleName(), field.getName());
        
        return result;
    }
    
    public Object format(Object source, Field field, Object fieldValue) {
        
        Object result;
        
        final javax.persistence.Temporal temporal;
        
        if(fieldValue instanceof Date 
                && (temporal = field.getAnnotation(javax.persistence.Temporal.class)) != null) {
            
            final Date date = (Date)fieldValue;
            
            result = this.getDateToStringConverter(temporal).convert(date);
            
        }else if(fieldValue instanceof java.time.temporal.Temporal) {  
            
            final java.time.temporal.Temporal t = (java.time.temporal.Temporal)fieldValue;
           
            result = this.getTemporalToStringConverter(t).convert(t);
            
        }else if(fieldValue != null && this.getTypeTests().isDomainType(field.getType())) {
            try{
                //@TODO The user's locale should be received as an argument
                result = entityToSelectOptionConverter.apply(fieldValue, Locale.ENGLISH);
            }catch(RuntimeException e) {
                LOG.warn("Failed to convert to SelectOption, value: " + fieldValue, e);
                result = fieldValue;
            }
        }else if(fieldValue != null && field.getType().isEnum()) {    
            
            result = this.domainTypeToIdConverter.convert(fieldValue);
            
        }else if (fieldValue instanceof Collection) {
            
            final Collection currentValue = (Collection)fieldValue;
            
            if( ! currentValue.isEmpty()) {
                
                final Collection update = (Collection)new ReflectionUtil()
                        .newInstanceForCollectionType(fieldValue.getClass());
                
//                result = currentValue.stream()
//                        .map((e) -> domainTypeToIdConverter.convert(e))
//                        .collect(Collectors.toCollection(() -> update));
                result = currentValue.stream()
                        .map((e) -> this.format(source, field, e))
                        .collect(Collectors.toCollection(() -> update));
                
                LOG.trace("For field: {}\nConverted: {}\n       To: {}", 
                        field, currentValue, result);
            }else{
                result = currentValue;
            }
        }else{
            result = fieldValue;
        }
        
        LOG.trace("Converted {} to {} for {}.{}", 
                fieldValue, result, field.getDeclaringClass().getSimpleName(), field.getName());
        
        return result;
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
                    throw Errors.unexpectedElement(temporalType, javax.persistence.TemporalType.values());
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
