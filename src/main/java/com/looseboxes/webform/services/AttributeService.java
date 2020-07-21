package com.looseboxes.webform.services;

import com.looseboxes.webform.store.StoreComposite;
import com.looseboxes.webform.Wrapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.ModelMap;
import com.looseboxes.webform.store.AttributeStore;
import com.looseboxes.webform.store.AttributeStoreProvider;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;
import com.looseboxes.webform.store.UnbackedStoreException;
import java.util.Collections;
import java.util.List;

/**
 * @author hp
 */
@Service
public class AttributeService 
        extends StoreComposite<String, Object> 
        implements Wrapper<StoreDelegate, AttributeService>{
//        implements AttributeStore<StoreDelegate>{
    
//    private static final Logger LOG = LoggerFactory.getLogger(AttributeService.class);
    
    private final AttributeStoreProvider attributeStoreProvider;
    
    @Nullable private StoreDelegate storeDelegate;

    @Autowired
    public AttributeService(AttributeStoreProvider provider) {
        this(provider, null);
    }
    
    public AttributeService(
            AttributeStoreProvider provider, 
            @Nullable StoreDelegate delegate) {
        super(delegate == null ? Collections.EMPTY_LIST : (List)provider.all(delegate));
        this.attributeStoreProvider = Objects.requireNonNull(provider);
        this.storeDelegate = delegate;
    }

    @Override
    public AttributeService wrap(StoreDelegate delegate) {
        return new AttributeService(this.attributeStoreProvider, delegate);
    }

    @Override
    @Nullable public StoreDelegate unwrap() {
        return this.storeDelegate;
    }

    public AttributeStore<ModelMap> modelAttributes() {
        if(storeDelegate == null) {
            throw new UnbackedStoreException(ModelMap.class);
        }
        return attributeStoreProvider.forModel(storeDelegate.getModelMap());
    }

    public AttributeStore<HttpServletRequest> requestAttributes() {
        if(storeDelegate == null) {
            throw new UnbackedStoreException(HttpServletRequest.class);
        }
        return attributeStoreProvider.forRequest(storeDelegate.getRequest());
    }

    public AttributeStore<HttpSession> sessionAttributes() {
        if(storeDelegate == null) {
            throw new UnbackedStoreException(HttpSession.class);
        }
        return attributeStoreProvider.forSession(storeDelegate.getRequest().getSession());
    }

    public AttributeStoreProvider getAttributeStoreProvider() {
        return attributeStoreProvider;
    }
}
