package com.looseboxes.webform.form;

import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import com.bc.webform.functions.IsFormFieldTestForJpaEntity;
import com.bc.webform.functions.TypeTests;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class FormFieldTestImpl 
        extends IsFormFieldTestForJpaEntity implements FormFieldTest{
 
    private static final Logger LOG = LoggerFactory.getLogger(FormFieldTestImpl.class);
    
    private final PropertySearch propertySearch;

    public FormFieldTestImpl(PropertySearch propertySearch, TypeTests typeTests) {
        super(typeTests);
        this.propertySearch = Objects.requireNonNull(propertySearch);
    }

    @Override
    public boolean test(Field field) {
        
        final List<String> fieldsToIgnore = this.propertySearch.findAll(
                WebformProperties.FIELDS_TO_IGNORE, field);
        
        LOG.trace("Fields to ignore for: {} = {}", field, fieldsToIgnore);
        
        final String [] fieldNames = this.propertySearch.getFieldNames(field);
        
        final Predicate<String> test = (fieldToIgnore) -> {
            for(String fieldName : fieldNames) {
                if(fieldToIgnore.equalsIgnoreCase(fieldName)) {
                    return true;
                }
            }
            return false;
        };
        
        final boolean ignore = fieldsToIgnore.stream().filter(test).findAny().isPresent();
        
        LOG.trace("Is field to ignore: {}, field: {}", ignore, field);
        
        final boolean output;
        
        if(ignore) {
        
            output = false;

        }else{
            
            output = super.test(field);
        }
        
        return output;
    }
}
