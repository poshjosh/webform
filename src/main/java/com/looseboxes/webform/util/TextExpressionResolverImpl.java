package com.looseboxes.webform.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class TextExpressionResolverImpl implements TextExpressionResolver {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(TextExpressionResolverImpl.class);
    
    private final TextExpressionMethods propertyExpressionMethods;

    public TextExpressionResolverImpl(
            TextExpressionMethods propertyExpressionMethods) {
        this.propertyExpressionMethods = Objects.requireNonNull(propertyExpressionMethods);
    }
    
    @Override
    public <R> R resolve(String text, Class<R> resultType, R resultIfNone) {

        if( ! this.isExpression(text)) {
            throw new IllegalArgumentException(text);
        }    

        final String methodName = this.getMethodNameFromExpression(text, resultType);

        final Method method = methodName == null ? null : getMethod(methodName, null);
        
        final Object result = method == null ? null : invokeMethod(method, null);
        
        LOG.debug("Resolved {} from expression {}", result, text);
        
        return result == null ? resultIfNone : (R)result;
    }
    
    public Object invokeMethod(Method method, Object resultIfNone) {
        
        Object result = null;
        
        final Class type = this.propertyExpressionMethods.getClass();
        try{
            result = method.invoke(this.propertyExpressionMethods);
        }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.warn("Failed to invoke " + method + " on type " + type, e);
        }
        
        return result == null ? resultIfNone : result;
    }
    
    public Method getMethod(String name, Method resultIfNone) {
        
        Method result = null;
        
        final Class type = this.propertyExpressionMethods.getClass();
        try{
            result = type.getMethod(name);
        }catch(NoSuchMethodException | SecurityException e) {
            LOG.warn("Method not found: " + name + ", on type " + type, e);
        }
        
        return result == null ? resultIfNone : result;
    }
    
    @Override
    public boolean isExpression(String text) {
        return text.startsWith("#");
    }
    
    public String getMethodNameFromExpression(String text, Class resultType) {

        // Remove the #
        final String methodPart = text.substring(1);
        
        final String methodName;
        
        switch(methodPart) {
            case "current_datetime":
                if(ZonedDateTime.class.equals(resultType)) {
                    methodName = "current_datetime_zoned";
                }else if(LocalDateTime.class.equals(resultType)) {
                    methodName = "current_datetime_local";
                }else if(Instant.class.equals(resultType)) {
                    methodName = "current_instant";
                }else{
                    methodName = methodPart;
                }
                break;
            case "current_date":
                if(LocalDate.class.equals(resultType)) {
                    methodName = "current_date_local";
                }else{
                    methodName = methodPart;
                }
                break;
            case "current_time":
                if(LocalTime.class.equals(resultType)) {
                    methodName = "current_time_local";
                }else{
                    methodName = methodPart;
                }
                break;
            case "current_timestamp":
                if(Instant.class.equals(resultType)) {
                    methodName = "current_instant";
                }else{
                    methodName = methodPart;
                }
                break;
            default:
                methodName = methodPart;
        }
        
        return methodName;
    }
}
