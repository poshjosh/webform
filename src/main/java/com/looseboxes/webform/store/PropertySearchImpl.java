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

import com.bc.jpa.spring.TypeFromNameResolver;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 20, 2019 9:15:32 AM
 */
public class PropertySearchImpl implements Serializable, PropertySearch {

    private static final Logger LOG = LoggerFactory.getLogger(PropertySearchImpl.class);
    
    private final String globalPrefix;
    
    private final PropertyStore store;

    private final TypeFromNameResolver typeFromNameResolver;
    
    public PropertySearchImpl(String prefix, PropertyStore store, 
            TypeFromNameResolver typeFromNameResolver) {
        this.globalPrefix = Objects.requireNonNull(prefix);
        this.store = Objects.requireNonNull(store);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
    }
    
    @Override
    public String findOrDefault(
            String propertyName, Class type, 
            String fieldName, String defaultValue) {

        final List<String> suffixes = this.buildSuffixes(type, fieldName);
        
        String val = null;
        
        for(String suffix : suffixes) {
        
            val = this.findOrDefault(propertyName, suffix, null);
            
            if(val != null) {
                break;
            }
        }
        
        LOG.trace("Value: {}, property: {}, entity: {}, field: {}",
                val, propertyName, type.getName(), fieldName);
        
        final String output = val == null ? defaultValue : val;
        
        return output;
    }
    
    private List<String> buildSuffixes(Class type, String fieldName) {

        final List<String> suffixes = new ArrayList(6);
        
        // form.dateTimeFormat.org.domain.Person.dateOfBirth
        suffixes.add(this.appendIfPresent(type.getName(), fieldName));
        
        // form.dateTimeFormat.Person.dateOfBirth
        suffixes.add(this.appendIfPresent(type.getSimpleName(), fieldName));
        
        final String typeName = this.typeFromNameResolver.getName(type);
        if( ! type.getSimpleName().equals(typeName)) {
            // form.dateTimeFormat.org.domain.${TYPE_NAME}.dateOfBirth
            suffixes.add(this.appendIfPresent(typeName, fieldName));
        }
        
        // If fieldName is null or empty then we have already attempted these
        // formats hence proceed only if fieldName is not null or empty
        if( ! this.isNullOrEmpty(fieldName)) {
            
            // form.dateTimeFormat.org.domain.Person
            suffixes.add(type.getName());
            
            // form.dateTimeFormat.Person
            suffixes.add(type.getSimpleName());
         
            if( ! type.getSimpleName().equals(typeName)) {
                // form.dateTimeFormat.org.domain.${TYPE_NAME}.dateOfBirth
                suffixes.add(typeName);
            }
        }
        
        if( ! this.isNullOrEmpty(fieldName)) {
            
            // form.dateTimeFormat.dateOfBirth
            suffixes.add(fieldName);
        }

        // form.dateTimeFormat
        suffixes.add(null);
        
        return Collections.unmodifiableList(suffixes);
    }    

    @Override
    public String findOrDefault(String propertyName, String suffix, String defaultValue) {
        Objects.requireNonNull(propertyName);
        if(suffix == null || suffix.isEmpty()) {
            return this.findOrDefault(propertyName, defaultValue);
        }else{
            final String val = this.find(propertyName + '.' + suffix).orElse(
                    this.findOrDefault(propertyName, null)
            );
            return val == null ? defaultValue : val;
        }
    }
    
    @Override
    public String findOrDefault(String propertyName, String defaultValue) {
        Objects.requireNonNull(propertyName);
        final String key = this.withPrefix(propertyName);
        final String val = getOrNull(key);
        return val == null ? defaultValue : val;
    }
    
    private String withPrefix(String s) {
        Objects.requireNonNull(s);
        return this.isNullOrEmpty(globalPrefix) ? s : globalPrefix + '.' + s;
    }

    private String appendIfPresent(String appendTo, String toAppend) {
        if(this.isNullOrEmpty(toAppend)) {
            return appendTo;
        }else{
            return appendTo + '.' + toAppend;
        }
    }
    
    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    private String getOrNull(String key) {
        return this.get(key, null);
    }

    private String get(String key, String resultIfNone) {
        final String val = this.store.getOrDefault(key, resultIfNone);
        LOG.trace("{} = {}", key, val);
        return val;
    }
}
