package com.looseboxes.webform.exceptions;

/**
 * @author hp
 */
public class ResourceNotFoundException extends RouteException {

    /**
     * Creates a new instance of <code>InvalidRouteException</code> without
     * detail message.
     */
    public ResourceNotFoundException() {
    }

    /**
     * Constructs an instance of <code>InvalidRouteException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ResourceNotFoundException(String msg) {
        super(msg);
    }

    public ResourceNotFoundException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public ResourceNotFoundException(Throwable thrwbl) {
        super(thrwbl);
    }
}
