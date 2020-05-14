/*
 * Copyright 2019 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.looseboxes.webform.store;

import java.lang.reflect.Field;
import java.util.Optional;
import javax.persistence.Column;

/**
 * Accesses property values in a predetermined format.
 * 
 * Allows for properties to be saved in hierarchies for example:
 * 
 * <code>
 * <pre>
 * p1.date
 * p1.type1.date
 * p1.type2.date
 * </pre>
 * </code>
 * 
 * <p><b>The standard for property access is defined by the following example:</b></p>
 * 
 * <code>
 * <pre>
 package org.domain;
 class Person{
      Date dateOfBirth;
 }
 
 String prefix = "form";
 
 PropertySearch instance;
 
 instance.find("dateTimeFormat", Person.class, "dateOfBirth");
 </pre>
 * </code>
 * 
 * <p>
 *     To get the value for the above property, property names are searched 
 *     in the environment, in the following order of preference:
 * </p>
 * 
 * <code>
 * <pre>
 * 
 * form.dateTimeFormat.org.domain.Person.dateOfBirth
 * 
 * form.dateTimeFormat.Person.dateOfBirth
 * 
 * form.dateTimeFormat.org.domain.Person
 * 
 * form.dateTimeFormat.Person
 * 
 * form.dateTimeFormat.dateOfBirth
 * 
 * form.dateTimeFormat
 * </pre> 
 * </code>
 * 
 * @author Chinomso Bassey Ikwuagwu on Apr 20, 2019 9:21:18 AM
 */
public interface PropertySearch{

    default Optional<String> find(String propertyName, Field field) {
        return Optional.ofNullable(this.findOrDefault(propertyName, field, null));
    }
    
    default String findOrDefault(String propertyName, Field field, String defaultValue) {
        final Class type = field.getDeclaringClass();
        String val = this.findOrDefault(propertyName, type, field.getName(), null);
        if(val == null) {
            final Column column = field.getAnnotation(Column.class);
            final String columnName = column == null ? null : column.name();
            if(columnName != null && !columnName.isEmpty()) {
                val = this.findOrDefault(
                propertyName, field.getDeclaringClass(), columnName, null);
            }
        }
        return val == null ? defaultValue : val;
    }

    default Optional<String> find(String propertyName, Class type) {
        return this.find(propertyName, type, null);
    }
    
    default String findOrDefault(String propertyName, Class type, String defaultValue) {
        return this.findOrDefault(propertyName, type, null, defaultValue);
    }
    
    default Optional<String> find(String propertyName, Class type, String fieldName) {
        return Optional.ofNullable(this.findOrDefault(propertyName, type, fieldName, null));
    }
    
    String findOrDefault(String propertyName, Class type, 
            String fieldName, String defaultValue);

    default Optional<String> find(String propertyName, String suffix) {
        return Optional.ofNullable(this.findOrDefault(propertyName, suffix, null));
    }
    
    String findOrDefault(String propertyName, String suffix, String defaultValue);

    default Optional<String> find(String propertyName) {
        return Optional.ofNullable(this.findOrDefault(propertyName, null));
    }
    
    String findOrDefault(String propertyName, String defaultValue);
}
