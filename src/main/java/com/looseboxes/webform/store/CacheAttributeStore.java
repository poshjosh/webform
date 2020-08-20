package com.looseboxes.webform.store;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.cache.Cache;

/**
 * A {@link com.looseboxes.webform.store.Store Store} implementation based on 
 * a {@link javax.cache.Cache}
 * @author hp
 */
public class CacheAttributeStore implements AttributeStore<Cache<String, Object>> {
    
    private static final Logger LOG = LoggerFactory.getLogger(CacheAttributeStore.class);
    
    private final Cache<String, Object> store;

    public CacheAttributeStore() {
        this.store = null;
    }
    
    public CacheAttributeStore(Cache<String, Object> delegate) {
        this.store = Objects.requireNonNull(delegate);
    }
    
    @Override
    public AttributeStore<Cache<String, Object>> wrap(Cache<String, Object> delegate) {
        return new CacheAttributeStore(delegate);
    }
    
    @Override
    public Cache<String, Object> unwrap() {
        return this.store;
    }

    @Override
    public Object put(String name, Object value) {
        final Object existing = this.remove(name);
        requireStore().put(name, value);
        LOG.trace("Put. {} = {}", name, value);
        return existing;
    }

    @Override
    public Object remove(String name) {
        final Object got = getOrDefault(name, null);
        requireStore().remove(name);
        LOG.trace("Removed. {} = {}", name, got);
        return got;
    }

    @Override
    public Object getOrDefault(String name, Object resultIfNone) {
        final Object got = (Object)requireStore().get(name);
        LOG.trace("Got. {} = {}", name, got);
        return got == null ? resultIfNone : got;
    }
    
    private Cache<String, Object> requireStore() {
        if(store == null) {
            throw new UnbackedStoreException(Cache.class);
        }
        return store;
    }
}
