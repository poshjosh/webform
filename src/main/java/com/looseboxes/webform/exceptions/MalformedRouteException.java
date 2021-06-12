package com.looseboxes.webform.exceptions;

/**
 * @author hp
 */
public class MalformedRouteException extends RouteException {

    /**
     * Creates a new instance of <code>MalformedRouteException</code> without
     * detail message.
     */
    public MalformedRouteException() {
    }

    /**
     * Constructs an instance of <code>MalformedRouteException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public MalformedRouteException(String msg) {
        super(msg);
    }
}
