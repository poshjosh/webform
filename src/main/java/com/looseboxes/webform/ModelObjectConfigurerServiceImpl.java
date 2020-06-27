package com.looseboxes.webform;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author hp
 */
public class ModelObjectConfigurerServiceImpl implements ModelObjectConfigurerService{
    
    private final Map<Class, ModelObjectConfigurer> configurers;

    public ModelObjectConfigurerServiceImpl() {
        this.configurers = new HashMap();
    }

    @Override
    public <T> Optional<ModelObjectConfigurer<T>> getConfigurer(Class<T> type) {
        return Optional.ofNullable(this.configurers.get(type));
    }

    @Override
    public <T> void setConfigurer(Class<T> type, ModelObjectConfigurer<T> configurer) {
        this.configurers.put(type, configurer);
    }
}
