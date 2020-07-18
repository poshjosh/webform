package com.looseboxes.webform.form;

import com.bc.webform.form.member.FormInputContextForJpaEntity;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class FormInputContextImpl extends FormInputContextForJpaEntity{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormInputContextImpl.class);
    
    private final PropertySearch propertySearch;

    public FormInputContextImpl(PropertySearch propertySearch) {
        this.propertySearch = Objects.requireNonNull(propertySearch);
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

    public PropertySearch getPropertySearch() {
        return propertySearch;
    }
}

