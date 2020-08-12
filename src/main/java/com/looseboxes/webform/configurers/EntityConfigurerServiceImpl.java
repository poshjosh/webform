package com.looseboxes.webform.configurers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author hp
 */
public class EntityConfigurerServiceImpl implements EntityConfigurerService{
    
    private final Map<Class, EntityConfigurer> configurers;

    public EntityConfigurerServiceImpl() {
        this.configurers = new HashMap();
    }

    @Override
    public <T> Optional<EntityConfigurer<T>> getConfigurer(Class<T> type) {
        
        EntityConfigurer entityConfigurer = this.configurers.get(type);
        
        if(entityConfigurer == null) {
            
            final Class assignableType = this.getAssignableType(type).orElse(null);
            
            if(assignableType != null) {
                
                entityConfigurer = this.configurers.get(assignableType);
            }
        }
        
        return Optional.ofNullable(entityConfigurer);
    }
    
    private Optional<Class> getAssignableType(final Class type) {
        return configurers.keySet().stream().filter((cls) -> cls.isAssignableFrom(type)).findAny();
    }

    @Override
    public <T> void setConfigurer(Class<T> type, EntityConfigurer<T> configurer) {
        this.configurers.put(type, configurer);
    }
}
