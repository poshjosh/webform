package com.looseboxes.webform.mappers;

import java.util.Optional;

/**
 * @author hp
 */
public interface EntityMapperService{

    <D, E> void setMapper(Class<D> dtoType, Class<E> entityType, EntityMapper<D, E> mapper);
    
    boolean isEntityType(Class type);

    boolean isDtoType(Class type);
    
    default <E> Object toDto(E entity){
        Class<E> entityType = (Class<E>)entity.getClass();
        return this.getMapperForEntity(entityType)
                .map((mapper) -> mapper.toDto(entity)).orElse(entity);
    }

    default <D> Object toEntity(D dto){
        Class<D> dtoType = (Class<D>)dto.getClass();
        return this.getMapperForDto(dtoType)
                .map((mapper) -> mapper.toEntity(dto)).orElse(dto);
    }
    
    <D> Optional<Class> getEntityType(Class<D> dtoType);
    
    <E> Optional<Class> getDtoType(Class<E> entityType);
    
    <D> Optional<EntityMapper<D, Object>> getMapperForDto(Class<D> dtoType);
    
    <E> Optional<EntityMapper<Object, E>> getMapperForEntity(Class<E> entityType);
}
