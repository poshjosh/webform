package com.looseboxes.webform.store;

import com.looseboxes.webform.Errors;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

/**
 * A {@link com.looseboxes.webform.store.Store Store} implementation based on 
 * a {@link javax.servlet.http.HttpServletRequest HttpServletRequest}
 * @author hp
 */
public class RequestAttributeStore implements AttributeStore<HttpServletRequest> {
    
    private final HttpServletRequest store;

    public RequestAttributeStore() {
        this.store = null;
    }
    
    public RequestAttributeStore(HttpServletRequest request) {
        this.store = Objects.requireNonNull(request);
    }
    
    @Override
    public AttributeStore<HttpServletRequest> wrap(HttpServletRequest request) {
        return new RequestAttributeStore(request);
    }
    
    @Override
    public HttpServletRequest unwrap() {
        return this.store;
    }

    @Override
    public Object put(String name, Object value) {
        final Object got = getOrDefault(name, null);
        requireStore().setAttribute(name, value);
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
    
    private HttpServletRequest requireStore() {
        if(store == null) {
            throw Errors.unbackedStore();
        }
        return store;
    }
}
