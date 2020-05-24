package com.looseboxes.webform.exceptions;

/**
 * @author hp
 */
public class InvalidRouteException extends RouteException {

    /**
     * Creates a new instance of <code>InvalidRouteException</code> without
     * detail message.
     */
    public InvalidRouteException() {
    }

    /**
     * Constructs an instance of <code>InvalidRouteException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidRouteException(String msg) {
        super(msg);
    }
}
