package com.looseboxes.webform.mappers;

/**
 * @author hp
 */
public interface EntityMapper<D, E> {
     
    E toEntity(D dto);

    D toDto(E entity);
}
