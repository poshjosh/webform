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

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import com.looseboxes.webform.repository.EntityRepositoryProvider;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:47:27 AM
 */
public class IdStringToDomainTypeConverterFactoryImpl 
        implements IdStringToDomainTypeConverterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(IdStringToDomainTypeConverterFactoryImpl.class);
 
    private final EntityRepositoryProvider repoFactory;

    public IdStringToDomainTypeConverterFactoryImpl(
            EntityRepositoryProvider entityRepositoryFactory) {
        this.repoFactory = Objects.requireNonNull(entityRepositoryFactory);
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        final Class srcClass = sourceType.getType();
        final Class tgtClass = targetType.getType();
        final boolean supported = String.class.equals(srcClass) && 
                this.repoFactory.isSupported(tgtClass);
        LOG.trace("Supported: {}, converting {} to {}", srcClass.getName(), tgtClass.getName());
        return supported;
    }
    
    @Override
    public Converter<String, Object> getConverter(Class targetType) {
        final Converter output;
        if(targetType.isEnum()) {
            output = new ConvertStringToEnum(targetType); 
        }else{
            output = new ConvertIdStringToEntity(repoFactory.forEntity(targetType));
        }
        return output;
    }
}