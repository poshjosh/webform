package com.looseboxes.webform.exceptions;

import org.springframework.lang.Nullable;

/**
 * @author hp
 */
public class AttributeNotFoundException extends MalformedRouteException {

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
