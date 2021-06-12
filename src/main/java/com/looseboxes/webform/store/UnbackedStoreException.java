package com.looseboxes.webform.store;

/**
 * This exception is thrown when an {@link com.looseboxes.webform.store.AttributeStore AttributesStore}
 * is unbacked by any actual delegate container.
 * @author hp
 */
public class UnbackedStoreException extends RuntimeException{

    public UnbackedStoreException() { }

    public UnbackedStoreException(Class expectedBackingType) { 
        super("You attempted to use a store that was not backed by any " + 
                expectedBackingType.getName() + 
                ". To create a backed store, call method AttributesStore.wrap(" + expectedBackingType.getSimpleName() + 
                ") and use the returned instance");
    }
    
    public UnbackedStoreException(String string) {
        super(string);
    }
}
