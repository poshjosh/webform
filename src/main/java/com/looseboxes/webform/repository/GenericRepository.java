package com.looseboxes.webform.repository;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.db.meta.access.MetaDataAccessImpl;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.functions.GetTableName;
import com.bc.jpa.spring.ConvertToType;
import com.bc.jpa.spring.EntityIdAccessor;
import com.bc.jpa.spring.EntityIdAccessorImpl;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class GenericRepository<E extends Object> implements EntityRepository<E, Object>{
    
    private static final Logger LOG = LoggerFactory.getLogger(GenericRepository.class);
    
    private final Class<E> entityType;
    
    private final JpaObjectFactory jpa;
    
    public GenericRepository(Class<E> entityType, JpaObjectFactory jpa) {
        this.entityType = Objects.requireNonNull(entityType);
        this.jpa = Objects.requireNonNull(jpa);
        LOG.debug("Entity type: {}", entityType);
    }

    @Override
    public void deleteById(Object id) {
// This will fail with message: 
// java.lang.IllegalArgumentException: Entity must be managed to call remove: 
// try merging the detached and try the remove again.        
//
// So we use the same Dao to find and then delete the entity
//        final Object found = this.find(id);
//        this.getDao().removeAndClose(found);
        try(final Dao dao = this.getDao()) {
            dao.begin();
            id = this.convertToIdTypeIfNeed(id);
            final Object found = dao.find(entityType, id);
            this.requireNotNull(found, id);
            dao.remove(found);
            dao.commit();
        }
    }

    @Override
    public <S extends E> S save(S entity) {
        final Optional idOptional = this.getIdOptional(entity);
        if(idOptional.isPresent()) {
            return this.merge(entity);
        }else{
            return this.persist(entity);
        }
    }
    
    public <S extends E> S persist(S entity) {
        this.getDao().persistAndClose(entity);
        return entity;
    }
    
    public <S extends E> S merge(S entity) {
        return this.getDao().mergeAndClose(entity);
    }

    @Override
    public Optional<Object> getIdOptional(E entity) {
        return this.getIdAccessor().getValueOptional(entity);
    }

    @Override
    public E findByIdOrException(Object id) throws EntityNotFoundException {
        return this.findById(id).orElseThrow(() -> new EntityNotFoundException());
    }

    @Override
    public Optional<E> findById(Object id) {
        final com.bc.jpa.dao.Dao dao = this.getDao();
        id = this.convertToIdTypeIfNeed(id);
        final E found = (E)dao.begin().findAndClose(entityType, id);
        return Optional.ofNullable(found);
    }

    @Override
    public List<E> findAllBy(String key, Object value, int offset, int limit) {
        return jpa.getDaoForSelect(entityType)
                .where(key, value).distinct(true)
                .getResultsAndClose(offset, limit);
    }

    private Collection<String> _uniqueColumns;
    @Override
    public Collection<String> getUniqueColumns() {
        
        if(_uniqueColumns == null) {
        
            final MetaDataAccess metaDataAccess = this.getMetaDataAccess();

            final String tableName = new GetTableName(metaDataAccess).apply(entityType);

            final List<String> indexNames = metaDataAccess
                    .fetchStringIndexInfo(tableName, true, MetaDataAccess.INDEX_NAME);
            
            if(indexNames == null || indexNames.isEmpty()) {
                _uniqueColumns = Collections.EMPTY_SET;
            }else{
                final List<String> columnNames = metaDataAccess
                        .fetchStringMetaData(tableName, MetaDataAccess.COLUMN_NAME);
                _uniqueColumns = indexNames.stream()
                        .filter((name) -> columnNames.contains(name))
                        .collect(Collectors.toSet());
            }
        }

        LOG.trace("Entity: {}, unique columns: {}", this.entityType.getName(), _uniqueColumns);

        return _uniqueColumns;
    }
    
    public Dao getDao() {
        return jpa.getDao();
    }
    
    public void requireNotNull(Object entity, Object id) 
            throws EntityNotFoundException{
        if(entity == null) {
            throw this.getNotFoundException(id);
        }
    }
    
    public EntityNotFoundException getNotFoundException(Object id) {
         return new EntityNotFoundException("Not found. " + 
                 entityType.getName() + ", searched by id: " + id);
    }
    
    public MetaDataAccess getMetaDataAccess() {
        final MetaDataAccess metaDataAccess = new MetaDataAccessImpl(
                this.getEntityManagerFactory());
        return metaDataAccess;
    }

    public Object convertToIdTypeIfNeed(Object id) {
        if( ! this.getPrimaryColumnType().isAssignableFrom(id.getClass())) {
            return this.getConvertToType().convert(id);
        }
        return id;
    }
    
    public String getPrimaryColumnName() {
        return this.getIdAccessor().getName(entityType);
    }
    
    public Class getPrimaryColumnType() {
        return this.getIdAccessor().getType(entityType);
    }
    
    private ConvertToType _c2t;
    public ConvertToType getConvertToType() {
        if(_c2t == null) {
            _c2t = new ConvertToType(this.getPrimaryColumnType());
        }
        return _c2t;
    }
    
    private EntityIdAccessor _eia;
    public EntityIdAccessor getIdAccessor() {
        if(_eia == null) {
            _eia = new EntityIdAccessorImpl(this.getEntityManagerFactory());
        }
        return _eia;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return this.jpa.getEntityManagerFactory();
    }
}
