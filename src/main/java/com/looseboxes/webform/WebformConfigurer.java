package com.looseboxes.webform;

/**
 * @author hp
 */
public interface WebformConfigurer {
    
    default void addModelObjectConfigurers(ModelObjectConfigurerService service) { }
}
