package com.looseboxes.webform.wip;

import java.util.Optional;
import java.util.function.Function;
import org.springframework.cache.Cache;

/**
 * @author hp
 */
public interface CacheProvider extends Function<String, Optional<Cache>>{

    @Override
    Optional<Cache> apply(String cacheName);
}
