package com.looseboxes.webform.domain;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author hp
 */
public interface GetUniqueColumnNames extends Function<Class, Collection<String>>{

    @Override
    Collection<String> apply(Class entityType);
}
