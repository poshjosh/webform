package com.looseboxes.webform.wip;

import com.looseboxes.webform.store.PropertySearch;
import java.util.Objects;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;

/**
 * @author hp
 */
public class CacheProviderImpl implements CacheProvider{
    
    private final ApplicationContext context;

    public CacheProviderImpl(ApplicationContext context) {
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public Optional<Cache> apply(String cacheName) {

        final Optional<Cache> output;

        if(cacheName == null) {
            output = Optional.empty();
        }else{
            final CacheManager cacheManager = context.getBean(CacheManager.class);
            if(cacheManager == null) {
                output = Optional.empty();
            }else{
                output = Optional.ofNullable(cacheManager.getCache(cacheName));
            }
        }

        return output;
    }
}
