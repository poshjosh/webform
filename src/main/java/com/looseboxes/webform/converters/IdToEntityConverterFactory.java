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

package com.looseboxes.webform.converters;

import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:47:27 AM
 */
public class IdToEntityConverterFactory implements ConverterFactory<String, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(IdToEntityConverterFactory.class);
 
    private final EntityRepositoryFactory entityRepositoryFactory;
    
    public IdToEntityConverterFactory(EntityRepositoryFactory entityRepositoryFactory) {
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
    }
    
    @Override
    public Converter<String, Object> getConverter(Class targetType) {

        final boolean supported = this.entityRepositoryFactory.isSupported(targetType);
        LOG.trace("Supports converting to: {} = {}", targetType.getSimpleName(), supported);
        
        final Converter output;
        if(supported) {
            if(targetType.isEnum()) {
                output = new ConvertIdToEnum(targetType);
            }else{
                output = new ConvertIdToEntity(
                        entityRepositoryFactory.forEntity(targetType));
            }
        }else{
            output = (toConvert) -> toConvert;
        }
        
        return output;
    }
}