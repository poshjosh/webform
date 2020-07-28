package com.looseboxes.webform.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class EntityMapperServiceImpl implements EntityMapperService{
    
    private static final Logger LOG = LoggerFactory.getLogger(EntityMapperServiceImpl.class);
    
    private final List<Class> dtoTypes = new ArrayList<>();
    private final List<Class> entityTypes = new ArrayList<>();
    private final List<EntityMapper> mappers = new ArrayList<>();

    @Override
    public <D, E> void setMapper(Class<D> dtoType, Class<E> entityType, EntityMapper<D, E> mapper) {
        this.dtoTypes.add(dtoType);
        this.entityTypes.add(entityType);
        this.mappers.add(mapper);
        LOG.debug("Added Mapper: {} for DTO: {}, Entity: {}", mapper, dtoType, entityType);
    }
    
    @Override
    public boolean isEntityType(Class type) {
        return this.entityTypes.indexOf(type) != -1;
    }

    @Override
    public boolean isDtoType(Class type) {
        return this.dtoTypes.indexOf(type) != -1;
    }

    @Override
    public <D> Object toEntity(D dto) {
        final Object entity = EntityMapperService.super.toEntity(dto);
        LOG.trace("Converted: {} to {}", dto, entity);
        return entity;
    }

    @Override
    public <E> Object toDto(E entity) {
        final Object dto = EntityMapperService.super.toDto(entity);
        LOG.trace("Converted: {} to {}", entity, dto);
        return dto;
    }

    @Override
    public <D> Optional<EntityMapper<D, Object>> getMapperForDto(Class<D> dtoType) {
        Optional<EntityMapper<D, Object>> found = this.get((List)dtoTypes, dtoType, mappers);
        LOG.trace("Found mapper: {} for DTO type: {}", found.orElse(null), dtoType);
        return found;
    }

    @Override
    public <E> Optional<EntityMapper<Object, E>> getMapperForEntity(Class<E> entityType) {
        Optional<EntityMapper<Object, E>> found = this.get((List)entityTypes, entityType, mappers);
        LOG.trace("Found mapper: {} for entity type: {}", found.orElse(null), entityType);
        return found;
    }

    @Override
    public <D> Optional<Class> getEntityType(Class<D> dtoType) {
        Optional<Class> found = this.get((List)dtoTypes, dtoType, entityTypes);
        LOG.trace("Found entity type: {} for DTO type: {}", found.orElse(null), dtoType);
        return found;
    }

    @Override
    public <E> Optional<Class> getDtoType(Class<E> entityType) {
        Optional<Class> found = this.get((List)entityTypes, entityType, dtoTypes);
        LOG.trace("Found DTO type: {} for entity type: {}", found.orElse(null), entityType);
        return found;
    }
    
    private <E, T> Optional<T> get(List<Class<E>> left, Class<E> leftElement, List<T> right) {
        final int index = left.indexOf(leftElement);
        return index == -1 ? Optional.empty() : Optional.ofNullable(right.get(index));
    }
}
