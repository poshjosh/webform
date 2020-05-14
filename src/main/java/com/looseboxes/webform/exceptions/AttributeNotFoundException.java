package com.looseboxes.webform.exceptions;

import io.micrometer.core.lang.Nullable;

/**
 * @author hp
 */
public class AttributeNotFoundException extends RuntimeException {

    public AttributeNotFoundException(@Nullable String model, String attributeName) {
        super(model == null || model.isEmpty() ? 
                "You did not specify a value for: " + attributeName :
                "You did not specify a value for: " + model + ' ' + attributeName);
    }

    /**
     * Constructs an instance of <code>AttributeNotFoundException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public AttributeNotFoundException(String msg) {
        super(msg);
    }
}
