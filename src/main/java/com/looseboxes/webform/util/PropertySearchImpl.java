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

import com.looseboxes.webform.store.PropertyStore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 20, 2019 9:15:32 AM
 */
public class PropertySearchImpl implements Serializable, PropertySearch {

    private static final Logger LOG = LoggerFactory.getLogger(PropertySearchImpl.class);
    
    private final String globalPrefix;
    
    private final PropertyStore store;

    private final PropertySuffixes propertySuffixes;
    
    private final String separator;

    public PropertySearchImpl(PropertyStore store, 
            PropertySuffixes propertySuffixes) {
        this("", store, propertySuffixes, ",");
    }
    
    public PropertySearchImpl(String prefix, PropertyStore store, 
            PropertySuffixes propertySuffixes, String separator) {
        this.globalPrefix = Objects.requireNonNull(prefix);
        this.store = Objects.requireNonNull(store);
        this.propertySuffixes = Objects.requireNonNull(propertySuffixes);
        this.separator = Objects.requireNonNull(separator);
    }
    
    @Override
    public List<String> findAll(
            String propertyName, Class type, String [] fieldNames) {
        final String found = this.findOrDefault(propertyName, type, fieldNames, null);
        return this.split(found);
    }
    
    @Override
    public String findOrDefault(
            String propertyName, Class type, 
            String [] fieldNames, String defaultValue) {

        final Collection<String> suffixes = this.propertySuffixes.from(type, fieldNames);
        
        final boolean hasSeparator = ! StringUtils.isNullOrEmpty(separator);
        
        String val = null;
        List<String> list = null;
        
        for(String suffix : suffixes) {
        
            final String found = this.findOrDefault(propertyName, suffix, null);
            
            if(StringUtils.isNullOrEmpty(found)) {
                continue;
            }
            
            if( ! hasSeparator) {
                val = found;
                break;
            }else{
                if(list == null) {
                    list = new ArrayList(suffixes.size());
                }
                if( ! list.contains(found)) {
                    list.add(found);
                }
//                val = val == null ? found : val + separator + found;
            }
        }
        
        if(hasSeparator && list != null) {
            val = list.stream().collect(Collectors.joining(separator));
        }
        
        LOG.trace("Value: {}, property: {}, entity: {}, fields: {}",
                val, propertyName, type.getName(), fieldNames);
        
        final String output = val == null ? defaultValue : val;
        
        return output;
    }

    @Override
    public List<String> findAll(String propertyName, String suffix) {
        final String found = findOrDefault(propertyName, suffix, null);
        return this.split(found);
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
    public List<String> findAll(String propertyName) {
        final String found = findOrDefault(propertyName, null);
        return this.split(found);
    }
    
    @Override
    public String findOrDefault(String propertyName, String defaultValue) {
        Objects.requireNonNull(propertyName);
        final String key = this.withPrefix(propertyName);
        final String val = getOrNull(key);
        return val == null ? defaultValue : val;
    }
    
    private List<String> split(String val) {
        if(StringUtils.isNullOrEmpty(separator)) {
            return Collections.singletonList(val);
        }else{
            return StringArrayUtils.toList(val, separator);
        }
    }

    private String withPrefix(String s) {
        Objects.requireNonNull(s);
        return StringUtils.isNullOrEmpty(globalPrefix) ? s : globalPrefix + '.' + s;
    }
    
    private String getOrNull(String key) {
        return this.get(key, null);
    }

    private String get(String key, String resultIfNone) {
        final String val = this.store.getOrDefault(key, resultIfNone);
//        LOG.trace("{} = {}", key, val);
        return val;
    }
}
