package com.looseboxes.webform.store;

import java.util.Objects;
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

    public AttributeStoreProviderImpl(
            RequestAttributeStore requestStore, 
            ModelAttributeStore modelStore, 
            SessionAttributeStore sessionStore) {
        this.requestStore = Objects.requireNonNull(requestStore);
        this.modelStore = Objects.requireNonNull(modelStore);
        this.sessionStore = Objects.requireNonNull(sessionStore);
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
}
