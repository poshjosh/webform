package com.looseboxes.webform.exceptions;

/**
 * @author hp
 */
public class RouteException extends RuntimeException{

    public RouteException() {}

    public RouteException(String string) {
        super(string);
    }

    public RouteException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public RouteException(Throwable thrwbl) {
        super(thrwbl);
    }
}
