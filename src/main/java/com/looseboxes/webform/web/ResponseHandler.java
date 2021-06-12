package com.looseboxes.webform.web;

/**
 *
 * @author hp
 */
public interface ResponseHandler<S, T> {

    T respond(S config);

    T respond(S config, Exception e);
}
