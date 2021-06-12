package com.looseboxes.webform.domain;

import java.beans.PropertyDescriptor;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * @author hp
 */
public class ModelUpdaterImpl implements ModelUpdater {
    
    private final Logger LOG = LoggerFactory.getLogger(ModelUpdaterImpl.class);
    
    /**
     * Update a model with values from another model (usually of the same type)
     *
     * For each property, an update is only performed if the source has a
     * value and the target has a different or {@code null} value.
     *
     * If no update is performed, returns an empty optional
     * 
     * @param from The model to source values from
     * @param to   The model whose values will be updated
     * @return     An optional containing the updated model, or an empty optional
     * if no update was performed on the target.
     */
    @Override
    public Optional<Object> update(Object from, Object to) {
        
        final BeanWrapper src = PropertyAccessorFactory.forBeanPropertyAccess(from);
        
        final BeanWrapper tgt = PropertyAccessorFactory.forBeanPropertyAccess(to);
        
        PropertyDescriptor [] d = tgt.getPropertyDescriptors();
        
        final String targetTypeName = to.getClass().getSimpleName();
        
        int updated = 0;
        
        for(PropertyDescriptor e : d) {
            
            final String name = e.getName();
            
            if(src.isReadableProperty(name) && tgt.isWritableProperty(name)) {
                
                Object srcValue = src.getPropertyValue(name);
                
                if(srcValue != null) {
                    
                    Object tgtValue = tgt.getPropertyValue(name);
                    
                    if( ! srcValue.equals(tgtValue)) {
                        
                        tgt.setPropertyValue(name, srcValue);
                        
                        ++updated;
                        
                        LOG.debug("Updated: {} to: {}, for {}#{}", 
                                tgtValue, srcValue, targetTypeName, name);
                    }else{
                    
                        LOG.trace("No need to update {}#{}", targetTypeName, name);
                    }
                }
            }else{
                LOG.trace("Not editable {}#{}", targetTypeName, name);
            }
        }

        return updated < 1 ? Optional.empty() : Optional.of(tgt.getWrappedInstance());
    }
}
