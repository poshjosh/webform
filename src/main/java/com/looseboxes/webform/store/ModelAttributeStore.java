package com.looseboxes.webform.store;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;

/**
 * A {@link com.looseboxes.webform.store.Store Store} implementation based on 
 * a {@link javax.servlet.http.HttpServletRequest HttpServletRequest}
 * @author hp
 */
public class ModelAttributeStore implements AttributeStore<ModelMap> {
    
    private static final Logger LOG = LoggerFactory.getLogger(ModelAttributeStore.class);
    
    private final ModelMap store;

    public ModelAttributeStore() {
        this.store = null;
    }
    
    public ModelAttributeStore(ModelMap request) {
        this.store = Objects.requireNonNull(request);
    }
    
    @Override
    public AttributeStore<ModelMap> wrap(ModelMap request) {
        return new ModelAttributeStore(request);
    }
    
    @Override
    public ModelMap unwrap() {
        return this.store;
    }

    @Override
    public Object put(String name, Object value) {
        final Object existing = this.remove(name);
        requireStore().addAttribute(name, value);
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
        final Object got = (Object)requireStore().getAttribute(name);
        LOG.trace("Got. {} = {}", name, got);
        return got == null ? resultIfNone : got;
    }
    
    private ModelMap requireStore() {
        if(store == null) {
            throw new UnbackedStoreException(ModelMap.class);
        }
        return store;
    }
}
