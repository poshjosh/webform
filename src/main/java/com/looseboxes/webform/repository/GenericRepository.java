package com.looseboxes.webform.repository;

import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.spring.ConvertToType;
import com.bc.jpa.spring.EntityIdAccessor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hp
 */
public class GenericRepository<E extends Object> implements EntityRepository<E, Object>{
    
    private static final Logger LOG = LoggerFactory.getLogger(GenericRepository.class);
    
    private final Class<E> entityType;
    
    private final JpaObjectFactory jpaObjectFactory;
    
    private final EntityIdAccessor entityIdAccessor;
    
    public GenericRepository(
            Class<E> entityType, 
            JpaObjectFactory jpa, 
            EntityIdAccessor entityIdAccessor) {
        this.entityType = Objects.requireNonNull(entityType);
        this.jpaObjectFactory = Objects.requireNonNull(jpa);
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

// DID NOT WORK
//        try(final Delete dao = this.getJpaObjectFactory().getDaoForDelete(entityType)) {
//            dao.begin();
//            id = this.convertToIdTypeIfNeed(id);
//            final Object found = dao.find(entityType, id);
//            this.requireNotNull(found, id);
//            dao.remove(found).commit();
//        }
        
        this.customDeleteById(entityType, this.getPrimaryColumnName(), id);
    }

    @Transactional(readOnly = false)
    private void customDeleteById(Class entityType, String name, Object value) {
        EntityManagerFactory emf = this.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        try{
            //https://en.wikibooks.org/wiki/Java_Persistence/Criteria
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaDelete cd = cb.createCriteriaDelete(entityType);
            Root root = cd.from(entityType);
            cd.where(cb.equal(root.get(name), value));
            Query query = em.createQuery(cd);
            // https://stackoverflow.com/questions/25821579/transactionrequiredexception-executing-an-update-delete-query            
            em.getTransaction().begin();
            int updateCount = query.executeUpdate();
            em.flush();
            em.getTransaction().commit();
        }finally{
            em.close();
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
        return jpaObjectFactory.getDaoForSelect(entityType)
                .where(key, value).distinct(true)
                .getResultsAndClose(offset, limit);
    }
    
    public Dao getDao() {
        return jpaObjectFactory.getDao();
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
        return this.jpaObjectFactory.getEntityManagerFactory();
    }

    public Class<E> getEntityType() {
        return entityType;
    }

    public JpaObjectFactory getJpaObjectFactory() {
        return jpaObjectFactory;
    }

    public EntityIdAccessor getEntityIdAccessor() {
        return entityIdAccessor;
    }
}
