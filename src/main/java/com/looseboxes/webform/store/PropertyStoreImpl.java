package com.looseboxes.webform.store;

import java.util.Objects;
import java.util.Properties;

/**
 * @author hp
 */
public class PropertyStoreImpl implements PropertyStore{
    
    private final Properties props;
    
    public PropertyStoreImpl(Properties props) {
        this.props = Objects.requireNonNull(props);
    }
    
    @Override
    public String getOrDefault(String key, String resultIfNone) {
        return props.getProperty(key, resultIfNone);
    }
}
