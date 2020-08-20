package com.looseboxes.webform.store;

import java.util.Objects;
import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.ModelMap;

/**
 * @author hp
 */
public class AttributeStoreProviderImpl implements AttributeStoreProvider {
    
    private final RequestAttributeStore requestStore;
    
    private final ModelAttributeStore modelStore;
    
    private final SessionAttributeStore sessionStore;
    
    private final CacheAttributeStore cacheStore;

    public AttributeStoreProviderImpl(
            RequestAttributeStore requestStore, 
            ModelAttributeStore modelStore, 
            SessionAttributeStore sessionStore,
            CacheAttributeStore cacheStore) {
        this.requestStore = Objects.requireNonNull(requestStore);
        this.modelStore = Objects.requireNonNull(modelStore);
        this.sessionStore = Objects.requireNonNull(sessionStore);
        this.cacheStore = Objects.requireNonNull(cacheStore);
    }
    
    @Override
    public AttributeStore<HttpServletRequest> forRequest(HttpServletRequest request) {
        return requestStore.wrap(request);
    }

    @Override
    public AttributeStore<ModelMap> forModel(ModelMap model) {
        return modelStore.wrap(model);
    }

    @Override
    public AttributeStore<HttpSession> forSession(HttpSession session) {
        return sessionStore.wrap(session);
    }

    @Override
    public AttributeStore<Cache<String, Object>> forCache(Cache<String, Object> cache) {
        return cacheStore.wrap(cache);
    }
}
