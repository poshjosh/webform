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
        
        type = getMatchingKey(type);
        
        final EntityConfigurer entityConfigurer = this.configurers.get(type);
        
        return Optional.ofNullable(entityConfigurer);
    }
    
    private Class getMatchingKey(Class type) {
        for(Class cls : this.configurers.keySet()) {
            if(cls.isAssignableFrom(type)) {
                type = cls;
                break;
            }
        }
        return type;
    }

    @Override
    public <T> void setConfigurer(Class<T> type, EntityConfigurer<T> configurer) {
        this.configurers.put(type, configurer);
    }
}
