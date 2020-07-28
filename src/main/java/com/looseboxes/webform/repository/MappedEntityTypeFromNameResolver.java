package com.looseboxes.webform.repository;

import com.looseboxes.webform.mappers.EntityMapperService;
import com.bc.jpa.spring.TypeFromNameResolver;
import java.util.Objects;

/**
 * @author hp
 */
public class MappedEntityTypeFromNameResolver implements TypeFromNameResolver{

    private final EntityMapperService entityMapperService;
    private final TypeFromNameResolver delegate;

    public MappedEntityTypeFromNameResolver(
            EntityMapperService entityMapperService, TypeFromNameResolver delegate) {
        this.entityMapperService = Objects.requireNonNull(entityMapperService);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public Object newInstance(Class type) {
        type = (Class)entityMapperService.getDtoType(type).orElse(type);
        return delegate.newInstance(type);
    }

    @Override
    public Class getType(String name, Class resultIfNone) {
        Class type = delegate.getType(name, resultIfNone);
        type = (Class)entityMapperService.getDtoType(type).orElse(type);
        return type;
    }

    @Override
    public String getName(Class type) {
        type = (Class)entityMapperService.getEntityType(type).orElse(type);
        return delegate.getName(type);
    }
}
