package com.looseboxes.webform.store;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link com.looseboxes.webform.store.Store Store} implementation based on 
 * a {@link javax.servlet.http.HttpServletRequest HttpServletRequest}
 * @author hp
 */
public class RequestAttributeStore implements AttributeStore<HttpServletRequest> {
    
    private static final Logger LOG = LoggerFactory.getLogger(ModelAttributeStore.class);
    
    private final HttpServletRequest store;

    public RequestAttributeStore() {
        this.store = null;
    }
    
    public RequestAttributeStore(HttpServletRequest delegate) {
        this.store = Objects.requireNonNull(delegate);
    }
    
    @Override
    public AttributeStore<HttpServletRequest> wrap(HttpServletRequest delegate) {
        return new RequestAttributeStore(delegate);
    }
    
    @Override
    public HttpServletRequest unwrap() {
        return this.store;
    }

    @Override
    public Object put(String name, Object value) {
        final Object got = getOrDefault(name, null);
        requireStore().setAttribute(name, value);
        LOG.trace("Put. {} = {}", name, value);
        return got;
    }

    @Override
    public Object remove(String name) {
        final Object got = getOrDefault(name, null);
        requireStore().removeAttribute(name);
        LOG.trace("Removed. {} = {}", name, got);
        return got;
    }

    @Override
    public Object getOrDefault(String name, Object resultIfNone) {
        final Object got = (Object)requireStore().getAttribute(name);
        LOG.trace("Got. {} = {}", name, got);
        return got == null ? resultIfNone : got;
    }
    
    private HttpServletRequest requireStore() {
        if(store == null) {
            throw new UnbackedStoreException(HttpServletRequest.class);
        }
        return store;
    }
}
