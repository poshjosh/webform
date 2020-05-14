package com.looseboxes.webform;

import static com.looseboxes.webform.CrudActionNames.CREATE;
import static com.looseboxes.webform.CrudActionNames.DELETE;
import static com.looseboxes.webform.CrudActionNames.READ;
import static com.looseboxes.webform.CrudActionNames.UPDATE;
import com.looseboxes.webform.store.SessionAttributeStore.StoreNotBackedBySessionException;
import com.looseboxes.webform.store.UnbackedStoreException;
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
    
    public static final UnbackedStoreException unbackedStore(){
        final String message = "You attempted to use a store that was not backed by any object. Rather, call method AttributesStore.wrap(...) and use the returned instance";
        return new StoreNotBackedBySessionException(message);
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
    
    public static final RuntimeException unexpectedAction(String action) {
        return unexpected("Action", action, CREATE, READ, UPDATE, DELETE);
    }

    public static final RuntimeException unexpected(Object id, Object found, Object...expected) {
        return new IllegalArgumentException(
                "Unexpected " + id + ", found: " + found + 
                ", but expected: " + Arrays.toString(expected));
    }
    
    public static final RuntimeException unexpectedModelName(String expected, String found) {
        return new IllegalArgumentException("Model name, expected: " + expected + 
                ", found: " + found + ". May be caused by filling multiple forms simultaenously");
    }
}
