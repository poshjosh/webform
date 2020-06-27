package com.looseboxes.webform;

import java.util.Optional;

/**
 * @author hp
 */
public interface ModelObjectConfigurerService {
    
    <T> Optional<ModelObjectConfigurer<T>> getConfigurer(Class<T> type);
    
    /**
     * Add the specified {@link com.looseboxes.webform.ModelObjectConfigurer ModelObjectConfigurer}
     * for the specified type.
     * 
     * If a {@link com.looseboxes.webform.ModelObjectConfigurer ModelObjectConfigurer} 
     * already exists, builds and returns a composed instance otherwise adds
     * the specified instance and returns it.
     * 
     * @param <T>
     * @param type The type of model object to apply the added 
     * {@link com.looseboxes.webform.ModelObjectConfigurer ModelObjectConfigurer} to
     * @param configurer The {@link com.looseboxes.webform.ModelObjectConfigurer ModelObjectConfigurer}
     * to add
     * @return The composite {@link com.looseboxes.webform.ModelObjectConfigurer ModelObjectConfigurer}
     * composed of all the configurers added fo the specified type.
     */
    default <T> ModelObjectConfigurer<T> addConfigurer(
            Class<T> type, ModelObjectConfigurer<T> configurer) {
        ModelObjectConfigurer<T> target = this.getConfigurer(type).orElse(null);
        if(target == null) {
            target = configurer;
        }else{
            target = target.andThen(configurer);
        }
        this.setConfigurer(type, target);
        return target;
    }
    
    <T> void setConfigurer(Class<T> type, ModelObjectConfigurer<T> configurer);
}
