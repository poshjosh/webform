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

package com.looseboxes.webform.repository;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 2:01:23 PM
 */
public interface EntityRepository<E, ID> {
    
    default E findByIdOrException(ID id) throws EntityNotFoundException {
        return findById(id).orElseThrow(() -> new EntityNotFoundException());
    }
    
    List<E> findAllBy(String key, Object value, int offset, int limit);
    
    Optional<E> findById(ID id);
    
    void deleteById(ID id);
    
    <S extends E> S save(S entity);
}
