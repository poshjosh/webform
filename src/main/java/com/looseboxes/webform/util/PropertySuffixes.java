package com.looseboxes.webform.util;

import com.bc.jpa.spring.TypeFromNameResolver;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class PropertySuffixes {
    
    private static final Logger LOG = LoggerFactory.getLogger(PropertySearchImpl.class);
    
    private final TypeFromNameResolver typeFromNameResolver;

    public PropertySuffixes(TypeFromNameResolver typeFromNameResolver) {
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
    }
    
    public Collection<String> from(Class type, String... fieldNames) {
        
        final int approx = 8;
        final int size = fieldNames == null || fieldNames.length == 0 ? approx : fieldNames.length * approx;
        // Use List, LinkedHashSet etc order of insertion important
        final Collection<String> result = 
                this.addSuffixes(type, fieldNames, new LinkedHashSet(size, 1.0f));

        LOG.trace("{}#{} suffixes: {}", type.getName(), fieldNames, result);
        
        return result;
    }
    
    private Collection<String> addSuffixes(Class type, String [] fieldNames, Collection<String> addTo) {

        final String typeName = type.getName();
        
        // form.dateTimeFormat.org.domain.Person.dateOfBirth
        if(fieldNames == null || fieldNames.length == 0) {
            addTo.add(typeName);
        }else{
            for(String fieldName : fieldNames) {
                addTo.add(this.appendIfPresent(typeName, fieldName));
            }
        }
        
        final String typeSimpleName = type.getSimpleName();
        
        // form.dateTimeFormat.Person.dateOfBirth
        if(fieldNames == null || fieldNames.length == 0) {
            addTo.add(typeSimpleName);
        }else{
            for(String fieldName : fieldNames) {
                addTo.add(this.appendIfPresent(typeSimpleName, fieldName));
            }
        }

        final String name = this.typeFromNameResolver.getName(type);
        if( ! typeSimpleName.equals(name)) {
            if(fieldNames == null || fieldNames.length == 0) {
                addTo.add(name);
            }else{
                // form.dateTimeFormat.org.domain.${TYPE_NAME}.dateOfBirth
                for(String fieldName : fieldNames) {
                    addTo.add(this.appendIfPresent(name, fieldName));
                }
            }
        }
        
        // If fieldName is null or empty then we have already attempted these
        // formats hence proceed only if fieldName is not null or empty
        if(fieldNames != null && fieldNames.length != 0) {
            // form.dateTimeFormat.org.domain.Person
            addTo.add(typeName);

            // form.dateTimeFormat.Person
            addTo.add(typeSimpleName);

            if( ! type.getSimpleName().equals(name)) {
                // form.dateTimeFormat.org.domain.${TYPE_NAME}.dateOfBirth
                addTo.add(name);
            }
        }

        if(fieldNames != null && fieldNames.length != 0) {
            for(String fieldName : fieldNames) {
                if( ! StringUtils.isNullOrEmpty(fieldName)) {

                    // form.dateTimeFormat.dateOfBirth
                    addTo.add(fieldName);
                }
            }
        }

        // form.dateTimeFormat
        addTo.add(null);
        
        LOG.trace("{} field names: {} suffixes: {}", type.getName(), 
                (fieldNames==null?null:Arrays.toString(fieldNames)), addTo);
        
        return addTo;
    }    

    private String appendIfPresent(String appendTo, String toAppend) {
        if(StringUtils.isNullOrEmpty(toAppend)) {
            return appendTo;
        }else{
            return appendTo + '.' + toAppend;
        }
    }
}
