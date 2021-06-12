package com.looseboxes.webform.configurers;

import java.util.Optional;

/**
 * @author hp
 */
public interface EntityConfigurerService {
    
    <T> Optional<EntityConfigurer<T>> getConfigurer(Class<T> type);
    
    /**
     * Add the specified {@link com.looseboxes.webform.configurers.EntityConfigurer EntityConfigurer}
     * for the specified type.
     * 
     * If a {@link com.looseboxes.webform.configurers.EntityConfigurer EntityConfigurer} 
     * already exists, builds and returns a composed instance otherwise adds
     * the specified instance and returns it.
     * 
     * @param <T>
     * @param type The type of model object to apply the added 
     * {@link com.looseboxes.webform.configurers.EntityConfigurer EntityConfigurer} to
     * @param configurer The {@link com.looseboxes.webform.configurers.EntityConfigurer EntityConfigurer}
     * to add
     * @return The composite {@link com.looseboxes.webform.configurers.EntityConfigurer EntityConfigurer}
     * composed of all the configurers added fo the specified type.
     */
    default <T> EntityConfigurer<T> addConfigurer(
            Class<T> type, EntityConfigurer<T> configurer) {
        EntityConfigurer<T> target = this.getConfigurer(type).orElse(null);
        if(target == null) {
            target = configurer;
        }else{
            target = target.andThen(configurer);
        }
        this.setConfigurer(type, target);
        return target;
    }
    
    <T> void setConfigurer(Class<T> type, EntityConfigurer<T> configurer);
}
