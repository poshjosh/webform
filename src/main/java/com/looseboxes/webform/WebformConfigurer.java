package com.looseboxes.webform;

import com.looseboxes.webform.entity.EntityConfigurerService;

/**
 * @author hp
 */
public interface WebformConfigurer {
    
    default void addModelObjectConfigurers(EntityConfigurerService service) { }
}
