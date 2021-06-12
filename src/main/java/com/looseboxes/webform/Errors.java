package com.looseboxes.webform;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;

/**
 * @author hp
 */
public class Errors {
    
    public static final RuntimeException methodAlreadyCalled(String method) {
        return new IllegalStateException("Method " + method + 
                " has already been called, and may only be called once");
    }
    
    public static final RuntimeException propertyValueNotFound(String name) {
        return new RuntimeException("Value not found for property: " + name);
    }
    
    public static final RuntimeException objectFieldMismatch(
            Object obj, Field field, Exception e) {
        return new IllegalStateException("Field " + field.getName() + 
                " is not a member of " + (obj == null ? null : obj.getClass()) + 
                ". Field details: " + field, e);
    }
    
    public static final RuntimeException modelOrModelMapRequired(Class type) {
        return new IllegalArgumentException("Found type: " + type + 
                ", expected any of: " + Arrays.toString(new Class[]{Model.class, ModelMap.class}));
    }
    
    public static final RuntimeException unexpected(Object found, Object...expected) {
        return new IllegalArgumentException(
                "Unexpected " + found.getClass().getName() + ", found: " + found + 
                ", but expected: " + Arrays.toString(expected));
    }
    
    public static final <T> RuntimeException unexpectedElement(Object found, T[]expected) {
        return new IllegalArgumentException(
                "Unexpected " + found.getClass().getName() + ", found: " + found + 
                ", but expected: " + Arrays.toString(expected));
    }

    public static final RuntimeException unexpectedModelName(String expected, String found) {
        return new IllegalArgumentException("Model name, expected: " + expected + 
                ", found: " + found + ". May be caused by filling multiple forms simultaenously");
    }
}
