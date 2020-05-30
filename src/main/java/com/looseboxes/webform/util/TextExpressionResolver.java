package com.looseboxes.webform.util;

/**
 *
 * @author hp
 */
public interface TextExpressionResolver {

    boolean isExpression(String text);
    
    <R> R resolve(String text, Class<R> resultType, R resultIfNone);
}
