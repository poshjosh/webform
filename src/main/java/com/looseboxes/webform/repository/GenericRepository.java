package com.looseboxes.webform.repository;

import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.spring.ConvertToType;
import com.bc.jpa.spring.EntityIdAccessor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    
    private final EntityIdAccessor entityIdAccessor;
    
    public GenericRepository(
            Class<E> entityType, 
            JpaObjectFactory jpa, 
            EntityIdAccessor entityIdAccessor) {
        this.entityType = Objects.requireNonNull(entityType);
        this.jpa = Objects.requireNonNull(jpa);
        this.entityIdAccessor = Objects.requireNonNull(entityIdAccessor);
        LOG.trace("Entity type: {}", entityType);
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
        final Optional idOptional = this.entityIdAccessor.getValueOptional(entity);
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

    public Object convertToIdTypeIfNeed(Object id) {
        if( ! this.getPrimaryColumnType().isAssignableFrom(id.getClass())) {
            return this.getConvertToType().convert(id);
        }
        return id;
    }
    
    public String getPrimaryColumnName() {
        return this.entityIdAccessor.getName(entityType);
    }
    
    public Class getPrimaryColumnType() {
        return this.entityIdAccessor.getType(entityType);
    }
    
    private ConvertToType _c2t;
    public ConvertToType getConvertToType() {
        if(_c2t == null) {
            _c2t = new ConvertToType(this.getPrimaryColumnType());
        }
        return _c2t;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return this.jpa.getEntityManagerFactory();
    }
}
