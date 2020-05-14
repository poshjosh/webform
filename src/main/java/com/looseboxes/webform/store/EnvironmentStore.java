package com.looseboxes.webform.store;

import java.util.Objects;
import org.springframework.core.env.Environment;

/**
 * @author hp
 */
public class EnvironmentStore implements PropertyStore {

    private final Environment environment;

    public EnvironmentStore(Environment environment) {
        this.environment = Objects.requireNonNull(environment);
    }
    
    @Override
    public String getOrDefault(String key, String resultIfNone) {
        return this.environment.getProperty(key, resultIfNone);
    }
}
