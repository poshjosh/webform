package com.looseboxes.webform.converters;

import com.looseboxes.webform.WebformProperties;
import java.util.Locale;
import java.util.Objects;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import com.looseboxes.webform.util.PropertySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.looseboxes.webform.util.StringUtils;
import java.util.List;

/**
 * @author hp
 */
public class DomainObjectPrinterImpl implements DomainObjectPrinter{

    private static final Logger LOG = LoggerFactory.getLogger(DomainObjectPrinterImpl.class);
    
    private final PropertySearch propertyAccess;

    public DomainObjectPrinterImpl(PropertySearch propertyAccess) {
        this.propertyAccess = Objects.requireNonNull(propertyAccess);
    }
    
    @Override
    public String print(Object object, Locale locale) {
        
        LOG.trace("Converting {} to java.lang.String for locale {}", object, locale);
        
        String output = null;
        
        if(object == null) {
            output = "";
        }else if(object instanceof Enum){
            output = object.toString();
        }else{
            
            final Class type = object.getClass();

            final List<String> defaultFieldNames = propertyAccess
                    .findAll(WebformProperties.DEFAULT_FIELDS, type);
            
            LOG.trace("Default field names to display: {}, for type: {}", 
                    defaultFieldNames, type.getName());

            final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(object);

            for(String fieldName : defaultFieldNames) {

                if(StringUtils.isNullOrEmpty(fieldName)) {
                    continue;
                }

                final Object value = ! bean.isReadableProperty(fieldName) ? 
                        null :  bean.getPropertyValue(fieldName);
                if(value != null) {
                    LOG.trace("Found {} = {} for {}", fieldName, value, object);
                    output = value.toString();
                    break;
                }
            }

            if(output == null) {
                output = object.toString();
            }
        }

        if(output == null) {
            output = object == null ? "" : object.toString();
        }
        
        LOG.trace("Converted: {} to: {}, using locale: {}", object, output, locale);
        
        return output;
    }
}

