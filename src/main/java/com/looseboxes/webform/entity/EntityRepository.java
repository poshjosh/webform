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

package com.looseboxes.webform.entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 2:01:23 PM
 */
public interface EntityRepository<E> {

    Collection<String> getUniqueColumns();
    
    Optional getIdOptional(Object entity);

    List<E> findAllBy(String key, Object value, int offset, int limit);

    E find(Object id) throws EntityNotFoundException;
    
    void create(E entity);

    void deleteById(Object id);
    
    void update(E entity);
}
/**
 * 
    Class getEntityType();
    
    String getTableName();
    
    long count();
    
    boolean hasRecords();

    Optional getIdOptional(Object entity);
        
    void create(E entity);
    
    boolean exists(Object id);
    
    boolean existsBy(String name, Object value);
        
    default List<E> findAllBy(Attribute key, Object value) {
        return findAllBy(key.getName(), value);
    }
    
    List<E> findAllBy(String key, Object value);
    
    List<E> findAllBy(String key, Object value, int offset, int limit);

    default E findSingleBy(Attribute key, Object value, E outputIfNone) 
            throws NonUniqueResultException{
        return findSingleBy(key.getName(), value, outputIfNone);
    }
    default E findSingleBy(String key, Object value, E outputIfNone) 
            throws NonUniqueResultException{
        E found = null;
        try{
            found = findSingleBy(key, value);
        }catch(NoResultException ignored) { }
        return found == null ? outputIfNone : found;
    }

    default E findSingleBy(Attribute key, Object value) 
            throws NoResultException, NonUniqueResultException{
        return findSingleBy(key.getName(), value);
    }
    
    E findSingleBy(String key, Object value) 
            throws NoResultException, NonUniqueResultException;
            
    List<E> findAll();
    
    List<E> findAll(int offset, int limit);
    
    E find(Object id) throws EntityNotFoundException;

    E findOrDefault(Object id, E resultIfNone);
    
    void deleteById(Object id);
    
    void deleteManagedEntity(E entity);

    void update(E entity);

    List<E> search(String query);
     
    
    
    public String getTableNameFromAnnotation(Class entityType, String resultIfNone) {
        
        final Table table = (Table)entityType.getAnnotation(Table.class);
        
        final String nameFromAnnotation = table == null ? null : table.name();
        
        return nameFromAnnotation == null ? resultIfNone : nameFromAnnotation;
    }
    
    public Collection<String> getUniqueColumns(String tableName) {

        final Set<String> uniqueColumns;

        final List<String> indexNames = metaDataAccess.fetchStringIndexInfo(tableName, true, MetaDataAccess.INDEX_NAME);
        if(indexNames == null || indexNames.isEmpty()) {
            uniqueColumns = Collections.EMPTY_SET;
        }else{
            final List<String> columnNames = metaDataAccess.fetchStringMetaData(tableName, MetaDataAccess.COLUMN_NAME);
            uniqueColumns = indexNames.stream().filter((name) -> columnNames.contains(name)).collect(Collectors.toSet());
        }

        LOG.trace("Table: {}, unique columns: {}", tableName, uniqueColumns);

        return uniqueColumns;
    }
 * 
 */
