package com.looseboxes.webform.configurers;

/**
 * @author hp
 */
public interface EntityMapper<D, E> {
     
    E toEntity(D dto);

    D toDto(E entity);
}
