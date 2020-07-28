package com.looseboxes.webform.converters;

import com.looseboxes.webform.WebformProperties;
import java.util.Locale;
import java.util.Objects;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import com.looseboxes.webform.util.PropertySearch;
import com.looseboxes.webform.util.StringArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.looseboxes.webform.util.StringUtils;
import java.util.List;

/**
 * @author hp
 */
public class DomainTypePrinterImpl implements DomainTypePrinter{

    private static final Logger LOG = LoggerFactory.getLogger(DomainTypePrinterImpl.class);
    
    private final PropertySearch propertyAccess;

    public DomainTypePrinterImpl(PropertySearch propertyAccess) {
        this.propertyAccess = Objects.requireNonNull(propertyAccess);
    }
    
    @Override
    public String print(Object object, Locale locale) {
        
        LOG.trace("Converting {} of type {} to java.lang.String for locale {}", 
                object, (object==null?null:object.getClass().getSimpleName()), locale);
        
        final String output;
        if(object == null) {
            output = "";
        }else if(object instanceof Enum){
            // toString will return null, for generic enum typs
            output = object.toString() != null ? object.toString() : ((Enum)object).name();
        }else{
            output = this.printFromProperties(object, object.toString());
        }
        
        LOG.trace("Converted: {} to: {}, using locale: {}", object, output, locale);
        
        return output;
    }
    
    private String printFromProperties(Object object, String resultIfNone) {
        
        String output = null;
        
        final Class type = object.getClass();

        final List<String> defaultFieldNames = propertyAccess
                .findAll(WebformProperties.DEFAULT_FIELDS, type);

        LOG.debug("Default field names to display: {}, for type: {}", 
                defaultFieldNames, type.getName());

        final BeanWrapper bean = this.getBeanWrapper(object);

        for(String fieldName : defaultFieldNames) {

            if(StringUtils.isNullOrEmpty(fieldName)) {
                continue;
            }

            final Object value;

            final String separator = ".";
            if(fieldName.contains(separator)) {

                final List<String> path = StringArrayUtils.toList(fieldName, separator);

                value = this.getValueOrNull(bean, path);

            }else{

                value = this.getValueOrNull(bean, fieldName);
            }

            if(value != null) {

                output = value.toString();

                break;
            }
        }
        
        return output == null ? resultIfNone : output;
    }
    
    private Object getValueOrNull(BeanWrapper bean, List<String> path) {
        Object curr = bean.getWrappedInstance();
        BeanWrapper currBean = bean;
        for(String node : path) {
            curr = this.getValueOrNull(currBean, node);
            if(curr != null){
                currBean = this.getBeanWrapper(curr);
            }else{
                currBean = null;
                break;
            }
        }
        return curr;
    }

    private Object getValueOrNull(BeanWrapper bean, String fieldName) {
        final Object value = ! bean.isReadableProperty(fieldName) ? 
                null :  bean.getPropertyValue(fieldName);
        if(value != null) {
            LOG.trace("Found {} = {} for {}", fieldName, value, bean.getWrappedInstance());
        }
        return value;
    }
    
    private BeanWrapper getBeanWrapper(Object object) {
        return PropertyAccessorFactory.forBeanPropertyAccess(object);
    }
}

