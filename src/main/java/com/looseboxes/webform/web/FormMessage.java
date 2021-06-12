package com.looseboxes.webform.web;

import java.io.Serializable;
import java.util.Objects;

/**
 * Encapsulates a object/field validation error.
 * 
 * The field 
 * {@link com.looseboxes.webform.web.FormMessage#fieldValue fieldValue}
 * is NOT considered in either the equals or hashCode methods.
 * @author hp
 */
public class FormMessage implements Serializable{
    
    private String objectName;
    private String fieldName;
    private Object fieldValue;
    private String message;

    public String getObjectName() {
        return objectName;
    }

    public FormMessage objectName(String objectName) {
        this.objectName = objectName;
        return this;
    }
    
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FormMessage fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public FormMessage fieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
        return this;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getMessage() {
        return message;
    }

    public FormMessage message(String message) {
        this.message = message;
        return this;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.objectName);
        hash = 29 * hash + Objects.hashCode(this.fieldName);
//        hash = 29 * hash + Objects.hashCode(this.fieldValue);
        hash = 29 * hash + Objects.hashCode(this.message);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FormMessage other = (FormMessage) obj;
        if (!Objects.equals(this.objectName, other.objectName)) {
            return false;
        }
        if (!Objects.equals(this.fieldName, other.fieldName)) {
            return false;
        }
//        if (!Objects.equals(this.fieldValue, other.fieldValue)) {
//            return false;
//        }
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FormMessage{" + "objectName=" + objectName + ", fieldName=" + fieldName + ", rejectedValue=" + fieldValue + ", message=" + message + '}';
    }
}
