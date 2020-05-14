package com.looseboxes.webform.store;

import com.looseboxes.webform.store.Store;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author hp
 */
public class StoreComposite<K, V> implements Store<K, V>{
    
    private final List<Store<K, V>> stores;

    public StoreComposite(Store<K, V>... stores) {
        this(stores == null ? Collections.EMPTY_LIST : Arrays.asList(stores));
    }
    
    public StoreComposite(List<Store<K, V>> stores) {
        this.stores = Collections.unmodifiableList(stores);
    }

    @Override
    public V put(K name, V value) {
        V output = null;
        for(Store<K, V> store : stores) {
            final V previous = store.put(name, value);
            if(previous != null) {
                output = previous;
            }
        }
        return output;
    }

    @Override
    public V remove(K name) {
        V output = null;
        for(Store<K, V> store : stores) {
            final V previous = store.remove(name);
            if(previous != null) {
                output = previous;
            }
        }
        return output;
    }

    @Override
    public V getOrDefault(K name, V resultIfNone) {
        V output = null;
        for(Store<K, V> store : stores) {
            final V previous = store.getOrDefault(name, null);
            if(previous != null) {
                output = previous;
            }
        }
        return output;
    }
}
