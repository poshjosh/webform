package com.looseboxes.webform.store;

import com.looseboxes.webform.Errors;
import java.util.Objects;
import org.springframework.ui.ModelMap;

/**
 * A {@link com.looseboxes.webform.store.Store Store} implementation based on 
 * a {@link javax.servlet.http.HttpServletRequest HttpServletRequest}
 * @author hp
 */
public class ModelAttributeStore implements AttributeStore<ModelMap> {
    
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
        final Object got = getOrDefault(name, null);
        requireStore().addAttribute(name, value);
        return got;
    }

    @Override
    public Object remove(String name) {
        final Object got = getOrDefault(name, null);
        requireStore().addAttribute(name, null);
        requireStore().remove(name);
        return got;
    }

    @Override
    public Object getOrDefault(String name, Object resultIfNone) {
        final Object got = (Object)requireStore().getAttribute(name);
        return got == null ? resultIfNone : got;
    }
    
    private ModelMap requireStore() {
        if(store == null) {
            throw Errors.unbackedStore();
        }
        return store;
    }
}
