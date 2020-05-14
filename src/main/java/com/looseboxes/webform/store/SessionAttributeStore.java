package com.looseboxes.webform.store;

import com.looseboxes.webform.Errors;
import java.util.Objects;
import javax.servlet.http.HttpSession;

/**
 * A {@link com.looseboxes.webform.store.Store Store} implementation based on 
 * a {@link javax.servlet.http.HttpSession HttpSession}
 * @author hp
 */
public class SessionAttributeStore implements AttributeStore<HttpSession> {
    
    public static class StoreNotBackedBySessionException extends UnbackedStoreException{
        public StoreNotBackedBySessionException() { }
        public StoreNotBackedBySessionException(String string) {
            super(string);
        }
    }
    
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
    public Object put(String name, Object Objectalue) {
        final Object got = getOrDefault(name, null);
        requireStore().setAttribute(name, Objectalue);
        return got;
    }

    @Override
    public Object remove(String name) {
        final Object got = getOrDefault(name, null);
        requireStore().removeAttribute(name);
        return got;
    }

    @Override
    public Object getOrDefault(String name, Object resultIfNone) {
        final Object got = (Object)requireStore().getAttribute(name);
        return got == null ? resultIfNone : got;
    }

    private HttpSession requireStore() {
        if(store == null) {
            throw Errors.unbackedStore();
        }
        return store;
    }
}
