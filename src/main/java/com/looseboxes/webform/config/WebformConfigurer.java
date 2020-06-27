package com.looseboxes.webform.config;

import com.looseboxes.webform.entity.EntityConfigurerService;

/**
 * @author hp
 */
public interface WebformConfigurer {
    
    default void addModelObjectConfigurers(EntityConfigurerService service) { }
}
