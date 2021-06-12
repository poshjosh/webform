package com.looseboxes.webform.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public final class FieldUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(FieldUtils.class);
    
    private FieldUtils(){}
    
    public static boolean hasOneOrMoreNonNullBeanProperties(Object bean) {
        final Class beanClass = bean.getClass();
        final Field [] fields = beanClass.getDeclaredFields();
        for(Field field : fields) {
            if( ! isBeanField(beanClass, field)) {
                continue;
            }
            final Object fieldValue = FieldUtils.getFieldValue(bean, field);
            if(fieldValue != null) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isBeanField(Class sourceClass, Field field) {
        try{
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), sourceClass);
            if(pd.getReadMethod() == null || pd.getWriteMethod() == null) {
                return false;
            }
        }catch(IntrospectionException e) {
            return false;
        }
        return true;
    }
    
    public static Object getFieldValue(Object source, Field field) {
        final boolean accessible = field.isAccessible();
        try{
            if( ! accessible) {
                field.setAccessible(true);
            }
            return field.get(source);
        }catch(IllegalAccessException | IllegalArgumentException | SecurityException e) {
            LOG.warn("Failed to access value of field: " + field + " on instance: " + source, e);
            throw new RuntimeException(e);
        }finally{
            if( ! accessible) {
                field.setAccessible(accessible);
            }
        }
    }

    public static void setFieldValue(Object source, Field field, Object value) {
        final boolean accessible = field.isAccessible();
        try{
            if( ! accessible) {
                field.setAccessible(true);
            }
            field.set(source, value);
        }catch(IllegalAccessException | IllegalArgumentException | SecurityException e) {
            LOG.warn("Failed to set value to: " + value + " for field: " + 
                    field + " on instance: " + source, e);
            throw new RuntimeException(e);
        }finally{
            if( ! accessible) {
                field.setAccessible(accessible);
            }
        }
    }
}
