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
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.domain.GetUniqueColumnNames;
import java.util.Collection;
import java.util.Objects;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 12, 2019 5:07:34 PM
 */
public class EntityUniqueColumnsValidator implements Validator {

    private static final Logger LOG = LoggerFactory.getLogger(EntityUniqueColumnsValidator.class);

    private final GetUniqueColumnNames getUniqueColumnNames;
    private final EntityRepositoryProvider repositoryFactory;

    public EntityUniqueColumnsValidator(
            GetUniqueColumnNames getUniqueColumnNames, 
            EntityRepositoryProvider repositoryFactory) {
        this.getUniqueColumnNames = Objects.requireNonNull(getUniqueColumnNames);
        this.repositoryFactory = Objects.requireNonNull(repositoryFactory);
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        boolean supports = repositoryFactory.isSupported(clazz);
        LOG.trace("Supports: {}, type: {}", supports, clazz);
        return supports;
    }

    @Override
    public void validate(Object object, Errors errors) {

        final Class entityType = object.getClass();
        
        final Collection<String> uniqueColumns = this.getUniqueColumnNames.apply(entityType);
        
        LOG.trace("Entity: {}, unique columns: {}", entityType.getSimpleName(), uniqueColumns);
        
        if( ! uniqueColumns.isEmpty()) {

            final EntityRepository repository = repositoryFactory.forEntity(entityType);

            for(String uniqueCol : uniqueColumns) {

                final Object columnValue = errors.getFieldValue(uniqueCol);

                if(LOG.isTraceEnabled()) {
                    LOG.trace("SELECT * FROM {} WHERE {} = {} LIMIT 0, 1",
                            entityType.getSimpleName(), uniqueCol, columnValue);
                }

                if(columnValue != null && !columnValue.toString().isEmpty() && 
                        ! repository.findAllBy(uniqueCol, columnValue, 0, 1).isEmpty()) {

                    //@todo provide messages.properties or validation_messages.properties
                    errors.rejectValue(uniqueCol, "Duplicate." + uniqueCol, "`" + uniqueCol + "` already exists");
                }
            }
        }
    }
}
