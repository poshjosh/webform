package com.looseboxes.webform.store;

import java.util.Map;

/**
 * @author hp
 */
public interface Store<K, V> extends ReadOnlyStore<K, V>{
    
    default void putAll(Map<K, V> values) {
        values.forEach((k, v) -> {
            put(k, v);
        });
    }
    
    V put(K name, V value);
    
    default void removeAll(K[]names) {
        for(K name : names) {
            remove(name);
        }
    }
            
    V remove(K name);
}
