package com.looseboxes.webform.entity;

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
        return Optional.ofNullable(this.configurers.get(type));
    }

    @Override
    public <T> void setConfigurer(Class<T> type, EntityConfigurer<T> configurer) {
        this.configurers.put(type, configurer);
    }
}
