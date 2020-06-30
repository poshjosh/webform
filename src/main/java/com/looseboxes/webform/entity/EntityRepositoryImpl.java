package com.looseboxes.webform.entity;

import com.bc.db.meta.access.MetaDataAccess;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class EntityRepositoryImpl implements EntityRepository{
    
    private static final Logger LOG = LoggerFactory.getLogger(EntityRepositoryImpl.class);
    
    private final com.bc.jpa.spring.repository.EntityRepository delegate;
    private final MetaDataAccess metaDataAccess;

    public EntityRepositoryImpl(
            com.bc.jpa.spring.repository.EntityRepository delegate, 
            MetaDataAccess metaDataAccess) {
        this.delegate = Objects.requireNonNull(delegate);
        this.metaDataAccess = Objects.requireNonNull(metaDataAccess);
    }

    @Override
    public Optional getIdOptional(Object entity) {
        return delegate.getIdOptional(entity);
    }

    @Override
    public void create(Object entity) {
        delegate.create(entity);
    }

    @Override
    public Object find(Object id) throws EntityNotFoundException {
        return delegate.find(id);
    }

    @Override
    public void deleteById(Object id) {
        delegate.deleteById(id);
    }

    @Override
    public List findAllBy(String key, Object value, int offset, int limit) {
        return delegate.findAllBy(key, value, offset, limit);
    }

    @Override
    public void update(Object entity) {
        delegate.update(entity);
    }

    @Override
    public Collection getUniqueColumns() {
        
        final String nameFromAnnotation = getTableNameFromAnnotation(delegate.getEntityType(), null);

        final String tableName;
        if(nameFromAnnotation != null) {
            tableName = nameFromAnnotation;
        }else{
            tableName = delegate.getTableName();
        }
        
        return this.getUniqueColumns(tableName);
    }

    private String getTableNameFromAnnotation(Class entityType, String resultIfNone) {
        
        final Table table = (Table)entityType.getAnnotation(Table.class);
        
        final String nameFromAnnotation = table == null ? null : table.name();
        
        return nameFromAnnotation == null ? resultIfNone : nameFromAnnotation;
    }
    
    private Collection<String> getUniqueColumns(String tableName) {

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
}
