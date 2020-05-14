package com.looseboxes.webform.store;

/**
 * This exception is thrown when an {@link com.bc.webform.app.properties.AttributesStore AttributesStore}
 * is unbacked by any actual delegate container.
 * @author hp
 */
public class UnbackedStoreException extends RuntimeException{

    public UnbackedStoreException() {
    }

    public UnbackedStoreException(String string) {
        super(string);
    }
}
