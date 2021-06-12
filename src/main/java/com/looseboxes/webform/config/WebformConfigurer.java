package com.looseboxes.webform.config;

import com.looseboxes.webform.configurers.EntityConfigurerService;
import com.looseboxes.webform.mappers.EntityMapperService;

/**
 * @author hp
 */
public interface WebformConfigurer {
    
    default void addEntityConfigurers(EntityConfigurerService configurerService) { }
    
    default void addEntityMappers(EntityMapperService mapperService) { }
}
