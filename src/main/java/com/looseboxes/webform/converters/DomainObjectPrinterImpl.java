package com.looseboxes.webform.converters;

import com.looseboxes.webform.WebformProperties;
import java.util.Locale;
import java.util.Objects;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import com.looseboxes.webform.util.PropertySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.looseboxes.webform.entity.EntityRepositoryProvider;
import com.looseboxes.webform.util.StringUtils;
import java.util.List;

/**
 * @author hp
 */
public class DomainObjectPrinterImpl implements DomainObjectPrinter{

    private static final Logger LOG = LoggerFactory.getLogger(DomainObjectPrinterImpl.class);
    
    private final PropertySearch propertyAccess;
    
    private final EntityRepositoryProvider repoFactory;

    public DomainObjectPrinterImpl(
            PropertySearch propertyAccess,
            EntityRepositoryProvider repoFactory) {
        this.propertyAccess = Objects.requireNonNull(propertyAccess);
        this.repoFactory = Objects.requireNonNull(repoFactory);
    }
    
    @Override
    public String print(Object object, Locale locale) {
        
        LOG.trace("Converting {} to java.lang.String");
        
        String output = null;
        
        if(object == null) {
            output = "";
        }else if(object instanceof Enum){
            output = ((Enum)object).name();
        }else{
            
            final Class type = object.getClass();

            final List<String> defaultFieldNames = propertyAccess
                    .findAll(WebformProperties.DEFAULT_FIELDS, type);

            final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(object);

            for(String fieldName : defaultFieldNames) {

                if(StringUtils.isNullOrEmpty(fieldName)) {
                    continue;
                }

                final Object value = bean.getPropertyValue(fieldName);
                if(value != null) {
                    output = value.toString();
                    break;
                }
            }

            if(output == null) {

                output = object.toString();
//                final Object id = repoFactory.forEntity(type)
//                        .getIdOptional(object).orElse(null);
//                if(id != null) {
//                    output = id.toString();
//                }
            }
        }

        if(output == null) {
            output = object == null ? "" : object.toString();
        }
        
        LOG.trace("Converted: {} to: {}, using locale: {}", object, output, locale);
        
        return output;
    }
}

