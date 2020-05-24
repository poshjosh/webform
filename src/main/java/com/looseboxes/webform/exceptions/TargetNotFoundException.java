package com.looseboxes.webform.exceptions;

/**
 * @author hp
 */
public class TargetNotFoundException extends RouteException {

    /**
     * Creates a new instance of <code>InvalidRouteException</code> without
     * detail message.
     */
    public TargetNotFoundException() {
    }

    /**
     * Constructs an instance of <code>InvalidRouteException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TargetNotFoundException(String msg) {
        super(msg);
    }

    public TargetNotFoundException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public TargetNotFoundException(Throwable thrwbl) {
        super(thrwbl);
    }
}
