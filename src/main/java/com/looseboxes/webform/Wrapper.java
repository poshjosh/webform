package com.looseboxes.webform;

/**
 * @author hp
 */
public interface Wrapper<S, T> {
    
    /**
     * @param delegate
     * @return A new instance of the calling object backed by the specified
     * delegate
     */
    T wrap(S delegate);
    
    S unwrap();
}
