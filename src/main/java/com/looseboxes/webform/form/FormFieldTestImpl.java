package com.looseboxes.webform.form;

import com.looseboxes.webform.util.StringArrayUtils;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import com.bc.webform.functions.IsFormFieldTestForJpaEntity;
import com.bc.webform.functions.TypeTests;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;
import javax.persistence.Column;
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
        
        final String arr = this.propertySearch.appendingInstance().find(
                WebformProperties.FIELDS_TO_IGNORE, field).orElse(null);
        
        final Predicate<String> test = (fieldToIgnore) -> {
            if(fieldToIgnore.equalsIgnoreCase(field.getName())) {
                return true;
            }
            final Column column = field.getAnnotation(Column.class);
            final String name = column == null ? null : column.name();
            return fieldToIgnore.equalsIgnoreCase(name);
        };
        
        final boolean ignore = StringArrayUtils
                .toStream(arr).filter(test).findAny().isPresent();
        
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
