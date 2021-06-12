package com.looseboxes.webform.exceptions;

/**
 * @author hp
 */
public class FormMemberNotFoundException extends MalformedRouteException {
    
    public static FormMemberNotFoundException from(Object form, String formMemberName) {
        final String msg = "FormMember: " + formMemberName + ", not found for form: " + form;
        return new FormMemberNotFoundException(msg);
    }

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
