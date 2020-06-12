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
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import java.util.Objects;
import javax.persistence.EnumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:47:27 AM
 */
public class IdToDomainTypeConverterFactoryImpl 
        implements IdToDomainTypeConverterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(IdToDomainTypeConverterFactoryImpl.class);
 
    private final EntityRepositoryFactory repoFactory;
    private final EnumType enumType;

    public IdToDomainTypeConverterFactoryImpl(
            EntityRepositoryFactory entityRepositoryFactory,
            PropertySearch propertySearch) {
        this(entityRepositoryFactory,
                EnumType.valueOf(propertySearch.findOrDefault(
                        WebformProperties.FIELD_ENUM_TYPE, EnumType.ORDINAL.name())));
    }

    public IdToDomainTypeConverterFactoryImpl(
            EntityRepositoryFactory entityRepositoryFactory, EnumType enumType) {
        this.repoFactory = Objects.requireNonNull(entityRepositoryFactory);
        this.enumType = Objects.requireNonNull(enumType);
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        final Class type = targetType.getType();
        final boolean supported = this.repoFactory.isSupported(type);
        LOG.trace("Supports converting to: {} = {}", type.getName(), supported);
        return supported;
    }
    
    @Override
    public Converter<String, Object> getConverter(Class targetType) {
        final Converter output;
        if(targetType.isEnum()) {
            output = new ConvertStringToEnum(targetType); 
        }else{
            output = new ConvertIdToEntity(repoFactory.forEntity(targetType));
        }
        return output;
    }
}