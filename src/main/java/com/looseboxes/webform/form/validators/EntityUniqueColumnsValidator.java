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

import com.looseboxes.webform.repository.EntityRepository;
import java.util.Collection;
import java.util.Objects;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import com.looseboxes.webform.repository.EntityRepositoryProvider;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 12, 2019 5:07:34 PM
 */
public class EntityUniqueColumnsValidator implements Validator {

//    private static final Logger LOG = LoggerFactory.getLogger(EntityUniqueColumnsValidator.class);

    private final EntityRepositoryProvider entityRepositoryFactory;

    public EntityUniqueColumnsValidator(EntityRepositoryProvider entityRepositoryFactory) {
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return this.entityRepositoryFactory.isSupported(clazz);
    }

    @Override
    public void validate(Object object, Errors errors) {

        final Class entityType = object.getClass();
        
        final EntityRepository repo = entityRepositoryFactory.forEntity(entityType);

        final Collection<String> uniqueColumns = repo.getUniqueColumns();

        for(String uniqueCol : uniqueColumns) {

            final Object columnValue = errors.getFieldValue(uniqueCol);

            if(columnValue != null && !columnValue.toString().isEmpty() && 
                    ! repo.findAllBy(uniqueCol, columnValue, 0, 1).isEmpty()) {

                //@todo provide messages.properties or validation_messages.properties
                errors.rejectValue(uniqueCol, "Duplicate." + uniqueCol, "`" + uniqueCol + "` already exists");
            }
        }
    }
}
