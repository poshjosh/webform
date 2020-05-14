package com.looseboxes.webform.store;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hp
 */
@Configuration
public class StoreConfiguration {
    
    @Bean public AttributeStoreProvider attributeStoreProvider() {
        return new AttributeStoreProviderImpl(
                this.requestAttributeStore(),
                this.modelAttributeStore(),
                this.sessionAttributeStore()
        );
    }
    
    /**
     * Return an un-initialized instance of {@link com.looseboxes.webform.store.RequestAttributeStore RequestAttributeStore}.
     * <p>
     * Do not use the returned instance directly, rather use it to create an 
     * instance that is initialized with a HttpServletRequest attribute as follows:
     * </p>
     * <code>
     * <pre>
 HttpServletRequest request;
 RequestAttributeStore store = requestAttributeStore().wrap(request)
 </pre>
     * </code>
     * @return An instance of {@link com.looseboxes.webform.store.RequestAttributeStore RequestAttributeStore}
     */
    @Bean public RequestAttributeStore requestAttributeStore() {
        return new RequestAttributeStore();
    }
    
    /**
     * Return an un-initialized instance of {@link com.looseboxes.webform.store.ModelAttributeStore ModelAttributeStore}.
     * <p>
     * Do not use the returned instance directly, rather use it to create an 
     * instance that is initialized with a ModelMap attribute as follows:
     * </p>
     * <code>
     * <pre>
 ModelMap model;
 SessionAttributeStore store = modelAttributeStore().wrap(model)
 </pre>
     * </code>
     * @return An instance of {@link com.looseboxes.webform.store.ModelAttributeStore ModelAttributeStore}
     */
    @Bean public ModelAttributeStore modelAttributeStore() {
        return new ModelAttributeStore();
    }
    
    /**
     * Return an un-initialized instance of {@link com.looseboxes.webform.store.SessionAttributeStore SessionAttributeStore}.
     * <p>
     * Do not use the returned instance directly, rather use it to create an 
     * instance that is initialized with a HttpSession attribute as follows:
     * </p>
     * <code>
     * <pre>
 HttpSession session;
 SessionAttributeStore store = sessionAttributeStore().wrap(session)
 </pre>
     * </code>
     * @return An instance of {@link com.looseboxes.webform.store.SessionAttributeStore SessionAttributeStore}
     */
    @Bean public SessionAttributeStore sessionAttributeStore() {
        return new SessionAttributeStore();
    }
}
