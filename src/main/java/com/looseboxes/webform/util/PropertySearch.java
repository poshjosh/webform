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

package com.looseboxes.webform.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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
    
    default boolean containsIgnoreCase(List<String> toSearchIn, Field field) {
        
        final String [] fieldNames = this.getFieldNames(field);
        
        final Predicate<String> test = (candidate) -> {
            for(String fieldName : fieldNames) {
                if(candidate.equalsIgnoreCase(fieldName)) {
                    return true;
                }
            }
            return false;
        };
        
        return toSearchIn.stream().filter(test).findAny().isPresent();
    
    }
    
    default Optional<String> find(String propertyName, Field field) {
        return Optional.ofNullable(findOrDefault(propertyName, field, null));
    }
    
    default String findOrDefault(String propertyName, Field field, String defaultValue) {
        final Class type = field.getDeclaringClass();
        final String [] names = getFieldNames(field);
        return this.findOrDefault(propertyName, type, names, defaultValue);
    }

    default List<String> findAll(String propertyName, Field field) {
        final Class type = field.getDeclaringClass();
        final String [] names = getFieldNames(field);
        return this.findAll(propertyName, type, names);
    }

    default String [] getFieldNames(Field field) {
        final String name1 = field.getName();
        final String name2;
        final Column column = field.getAnnotation(Column.class);
        final String columnName = column == null ? null : column.name();
        if(columnName != null && !columnName.isEmpty() && !name1.equals(columnName)) {
            name2 = columnName;
        }else {
            final String snake_case = StringUtils.camelToSnakeCase(name1);
            if( ! snake_case.equals(name1)) {
                name2 = snake_case;
            }else{
                name2 = null;
            }
        }
        return name2 == null ? new String[]{name1} : new String[]{name1, name2};
    }
    
    default Optional<String> find(String propertyName, Class type) {
        return find(propertyName, type, null);
    }
    
    default String findOrDefault(String propertyName, Class type, String defaultValue) {
        return findOrDefault(propertyName, type, (String[])null, defaultValue);
    }
    
    default List<String> findAll(String propertyName, Class type) {
        return findAll(propertyName, type, (String[])null);
    }

    default Optional<String> find(String propertyName, Class type, String fieldName) {
        return Optional.ofNullable(findOrDefault(propertyName, type, fieldName, null));
    }
    
    default String findOrDefault(String propertyName, Class type, 
            String fieldName, String defaultValue) {
        return findOrDefault(propertyName, type,
                fieldName == null ? (String[])null : new String[]{fieldName}, defaultValue);
    }
    
    default List<String> findAll(String propertyName, Class type, String fieldName) {
        return findAll(propertyName, type,
                fieldName == null ? (String[])null : new String[]{fieldName});
    }

    String findOrDefault(String propertyName, Class type, 
            String [] fieldNames, String defaultValue);
    
    List<String> findAll(String propertyName, Class type, String [] fieldNames);

    default Optional<String> find(String propertyName, String suffix) {
        return Optional.ofNullable(findOrDefault(propertyName, suffix, null));
    }
    
    String findOrDefault(String propertyName, String suffix, String defaultValue);
    
    List<String> findAll(String propertyName, String suffix);

    default Optional<String> find(String propertyName) {
        return Optional.ofNullable(findOrDefault(propertyName, null));
    }
    
    String findOrDefault(String propertyName, String defaultValue);
    
    List<String> findAll(String propertyName);
}
