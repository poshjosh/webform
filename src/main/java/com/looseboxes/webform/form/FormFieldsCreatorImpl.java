package com.looseboxes.webform.form;

import com.bc.reflection.ReflectionUtil;
import com.looseboxes.webform.store.PropertySearch;
import com.bc.webform.Form;
import com.bc.webform.FormField;
import com.bc.webform.FormFieldBuilder;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.EntityToIdConverter;
import com.bc.webform.functions.CreateFormFieldsFromAnnotatedPersistenceEntity;
import com.bc.webform.functions.TypeTests;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public class FormFieldsCreatorImpl extends 
        CreateFormFieldsFromAnnotatedPersistenceEntity{

    private static final Logger LOG = LoggerFactory.getLogger(FormFieldsCreatorImpl.class);
    
    private final PropertySearch propertySearch;
    private final FormFieldChoices formFieldChoices;
    private final Comparator<FormField> formFieldComparator;
    private final DateToStringConverter dateToStringConverter;
    private final EntityToIdConverter entityToIdConverter;

    public FormFieldsCreatorImpl(
            FormFieldTest isFormField, 
            PropertySearch propertySearch,
            FormFieldChoices formFieldChoices,
            Comparator<FormField> formFieldComparator,
            DateToStringConverter dateToStringConverter,
            EntityToIdConverter entityToIdConverter) {
        super(isFormField, -1);
        this.propertySearch = Objects.requireNonNull(propertySearch);
        this.formFieldChoices = Objects.requireNonNull(formFieldChoices);
        this.formFieldComparator = Objects.requireNonNull(formFieldComparator);
        this.dateToStringConverter = Objects.requireNonNull(dateToStringConverter);
        this.entityToIdConverter = Objects.requireNonNull(entityToIdConverter);
    }

    @Override
    public Object getValue(Form form, Object object, Field field) {
        
        Object value = super.getValue(form, object, field);
        
        final Temporal temporal;
        
        if(value instanceof Date && (temporal = field.getAnnotation(Temporal.class)) != null) {
            
            final Date date = (Date)value;
            
            value = this.getDateToStringConverter(temporal).convert(date);
            
        }else if(this.getTypeTests().isDomainType(field.getType())) {

            value = this.entityToIdConverter.convert(value);
            
        }else if (this.isMultiChoice(form, object, field) &&
                this.isMultiValue(form, object, field) && 
                value instanceof Collection) {
            
            final Collection currentValue = (Collection)value;
            if( ! currentValue.isEmpty()) {
                final Collection update = (Collection)new ReflectionUtil()
                        .newInstanceForCollectionType(value.getClass());
                value = currentValue.stream()
                        .map((e) -> entityToIdConverter.convert(e))
                        .collect(Collectors.toCollection(() -> update));
                
                LOG.trace("For field: {}\nConverted: {}\n       To: {}", 
                        field, currentValue, value);
            }
        }
        
        LOG.trace("Value: {}, field: {}.{}", value, 
                field.getDeclaringClass().getName(), field.getName());
        
        return value;
    }
    
    public Converter<Date, String> getDateToStringConverter(Temporal temporal) {
        Converter<Date, String> selected;
        final TemporalType temporalType = temporal.value();
        if(temporalType != null) {
            switch(temporalType) {
                case DATE:
                    selected = dateToStringConverter.dateInstance(); break;
                case TIME:
                    selected = dateToStringConverter.timeInstance(); break;
                case TIMESTAMP:
                    selected = dateToStringConverter.datetTimeInstance(); break;
                default:
                    throw Errors.unexpected("TemporalType", 
                            temporalType, (Object[])TemporalType.values());
            }
        }else{
            LOG.warn("Unable to deduce temporal type of @Temporal annotation. Date will be converted via toString() method. Temporal: {}", temporal);
            selected = (date) -> date.toString();
        }    
        return selected;
    }

    @Override
    public String getType(Form form, Object object, Field field) {

        String type = this.getTypeFromProperties(form, object, field, null);
        
        if(type == null) {
            
            type = super.getType(form, object, field);
        }
        
        LOG.trace("Type: {}, field: {}.{}", type, 
                field.getDeclaringClass().getName(), field.getName());
        
        return type;
    }    
    
    /**
     * @param form
     * @param object
     * @param field
     * @param defaultValue
     * @return defaultValue of none, otherwise return either: text, number, 
     * password, file, checkbox, radio, datetime-local, date, time, hidden
     */
    public String getTypeFromProperties(
            Form form, Object object, Field field, String defaultValue) {
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

    @Override
    protected FormField buildFormField(Form form, Object object, 
            Field field, FormFieldBuilder builder) {
        this.propertySearch.find(WebformProperties.LABEL, field)
                .ifPresent((label) -> builder.label(label));
        this.propertySearch.find(WebformProperties.ADVICE, field)
                .ifPresent((advice) -> builder.advice(advice));
        final FormField formField = super.buildFormField(form, object, field, builder); 
        LOG.trace("{}", formField);
        return formField;
    }
    
    @Override
    public Comparator<FormField> getComparatorForReferenceFormFields() {
        return this.formFieldComparator;
    }

    @Override
    public Optional<Class> getReferenceType(Form form, Object object, Field field) {
        final Optional<Class> refType = super.getReferenceType(form, object, field);
        LOG.trace("Ref type: {}, field: {}", refType, field);
        return refType;
    }

    @Override
    public boolean isMultiChoice(Form form, Object object, Field field) {
        final Class type = field.getType();
        final boolean output;
        if(this.getTypeTests().isDomainType(type) && 
                this.formFieldChoices.hasValues(form, object, field)) {
            output = true;
        }else{
            output = super.isMultiChoice(form, object, field);
        }
        LOG.trace("Multichoice: {}, field: {}", output, field);
        return output;
    }

    @Override
    public Map getChoices(Form form, Object object, Field field) {
        
        Map output = null;
        
        if(this.isMultiChoice(form, object, field)) {
            
            final Class type = field.getType();

            final TypeTests typeTests = this.getTypeTests();

            if(typeTests.isDomainType(type) || typeTests.isEnumType(type)) {
                
                output = this.formFieldChoices.getChoices(form, object, field);
            }
        }
        
        if(output == null || output.isEmpty()) {
            
            output = super.getChoices(form, object, field);
        }
        
        LOG.trace("Choices: {}, field: {}.{}", output, 
                field.getDeclaringClass().getName(), field.getName());
        
        return output == null ? Collections.EMPTY_MAP : output;
    }
}
