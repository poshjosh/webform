package com.looseboxes.webform.exceptions;

/**
 * @author hp
 */
public class FormMemberNotFoundException extends RuntimeException {

    /**
     * Creates a new instance of <code>FormUpdateException</code> without detail
     * message.
     */
    public FormMemberNotFoundException() {
    }

    /**
     * Constructs an instance of <code>FormUpdateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public FormMemberNotFoundException(String msg) {
        super(msg);
    }
}
