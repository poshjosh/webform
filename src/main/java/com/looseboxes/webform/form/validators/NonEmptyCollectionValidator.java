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

package com.looseboxes.webform.form.validators;

import java.util.Collection;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 20, 2019 10:13:59 AM
 */
public class NonEmptyCollectionValidator implements Validator {
    
    private final String [] fieldList;

    public NonEmptyCollectionValidator() {
        this(new String[0]);
    }

    public NonEmptyCollectionValidator(String... fieldList) {
        this.fieldList = fieldList;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
        
        if(fieldList == null || fieldList.length == 0) {
            return;
        }
        
        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(target);
        
        for(String field : fieldList) {
        
            if(bean.isReadableProperty(field)) {
            
                final Class type = bean.getPropertyType(field);
                
                if(Collection.class.isAssignableFrom(type)) {
                
                    final Collection c = (Collection)bean.getPropertyValue(field);
                    
                    if(c == null || c.isEmpty()) {

                        //@todo provide messages.properties or validation_messages.properties
                        errors.rejectValue(field, "Empty." + field, "No values were selected");
                    }
                }
            }
        }
    }
}
