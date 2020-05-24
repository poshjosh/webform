package com.looseboxes.webform.exceptions;

/**
 * Thrown when a referenced form tries to update its parent form and fails 
 * @author hp
 */
public class FormUpdateException extends RuntimeException {

    /**
     * Creates a new instance of <code>FormUpdateException</code> without detail
     * message.
     */
    public FormUpdateException() {
    }

    /**
     * Constructs an instance of <code>FormUpdateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public FormUpdateException(String msg) {
        super(msg);
    }
}
