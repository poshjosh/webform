package com.looseboxes.webform.store;

import java.util.Optional;

/**
 * @author hp
 */
public interface ReadOnlyStore<K, V> {
    
    default Optional<V> getOptional(K name) {
        return Optional.ofNullable(getOrDefault(name, null));
    }
    
    V getOrDefault(K name, V resultIfNone);
}
