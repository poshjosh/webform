package com.looseboxes.webform.domain;

import java.util.Optional;

/**
 * @author hp
 */
public interface ModelUpdater {

    /**
     * Update a model with values from another model (usually of the same type)
     *
     * For each property, an update is only performed if the source has a
     * value and the target has a different or {@code null} value.
     *
     * If no update is performed, returns an empty optional
     * 
     * @param from The model to source values from
     * @param to   The model whose values will be updated
     * @return     An optional containing the updated model, or an empty optional
     * if no update was performed on the target.
     */
    Optional<Object> update(Object from, Object to);
}
