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

import com.bc.jpa.spring.repository.EntityRepository;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 20, 2019 1:15:23 PM
 */
public class ConvertIdToEntity implements Converter<String, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertIdToEntity.class);

    private final EntityRepository entityRepository;

    public ConvertIdToEntity(EntityRepository entityRepository) {
        this.entityRepository = Objects.requireNonNull(entityRepository);
    }
    
    @Override
    public Object convert(String toConvert) {
        try{
            final Object update = entityRepository.find(toConvert);
            LOG.trace("Converted: {} to: {}", toConvert, update);
            return update;
        }catch(RuntimeException e) {
            LOG.debug("Failed to convert {}, reason: {}", toConvert, e.toString());
            return toConvert;
        }
    }
}
