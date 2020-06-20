package com.looseboxes.webform.store;

import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link com.looseboxes.webform.store.Store Store} implementation based on 
 * a {@link javax.servlet.http.HttpSession HttpSession}
 * @author hp
 */
public class SessionAttributeStore implements AttributeStore<HttpSession> {

    private static final Logger LOG = LoggerFactory.getLogger(SessionAttributeStore.class);
    
    private final HttpSession store;

    public SessionAttributeStore() {
        this.store = null;
    }
    
    public SessionAttributeStore(HttpSession session) {
        this.store = Objects.requireNonNull(session);
    }
    
    @Override
    public AttributeStore<HttpSession> wrap(HttpSession session) {
        return new SessionAttributeStore(session);
    }
    
    @Override
    public HttpSession unwrap() {
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

    private HttpSession requireStore() {
        if(store == null) {
            throw new UnbackedStoreException(HttpSession.class);
        }
        return store;
    }
}
