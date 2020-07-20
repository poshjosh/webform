package com.looseboxes.webform.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public List<AttributeStore> all(StoreDelegate delegate) {
        final List<AttributeStore> list = new ArrayList(3);
        if(delegate.getModelMap() != null){
            list.add(this.forModel(delegate.getModelMap()));
        }
        if(delegate.getRequest() != null) {
            list.add(this.forRequest(delegate.getRequest()));
            list.add(this.forSession(delegate.getRequest().getSession()));
        }
        return Collections.unmodifiableList(list);
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
