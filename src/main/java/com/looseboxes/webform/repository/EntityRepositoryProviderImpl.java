package com.looseboxes.webform.repository;

import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.spring.EntityIdAccessor;
import com.bc.jpa.spring.util.JpaUtil;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author hp
 */
public class EntityRepositoryProviderImpl implements EntityRepositoryProvider{

//    private final Logger log = LoggerFactory.getLogger(EntityRepositoryProviderImpl.class);
    
    private final JpaObjectFactory jpaObjectFactory;
    
    private final Predicate<Class> domainTypeTest;
    
    private final EntityIdAccessor entityIdAccessor;
    
    public EntityRepositoryProviderImpl(
            JpaObjectFactory jpaObjectFactory, 
            Predicate<Class> domainTypeTest,
            EntityIdAccessor entityIdAccessor) {
        this.jpaObjectFactory = Objects.requireNonNull(jpaObjectFactory);
        this.domainTypeTest = Objects.requireNonNull(domainTypeTest);
        this.entityIdAccessor = Objects.requireNonNull(entityIdAccessor);
    }
    
    @Override
    public boolean isSupported(Class entityType) {
        entityType = JpaUtil.deduceActualDomainType(entityType);
        return domainTypeTest.test(entityType);
    }

    @Override
    public Optional<Object> getIdOptional(Object entity) {
        return this.entityIdAccessor.getValueOptional(entity);
    }
    
    @Override
    public <E> EntityRepository<E, Object> forEntity(Class<E> entityType) {
        entityType = JpaUtil.deduceActualDomainType(entityType);
        return new GenericRepository(entityType, this.jpaObjectFactory, this.entityIdAccessor);
    }
    
    public Predicate<Class> getDomainTypeTest() {
        return domainTypeTest;
    }

    public JpaObjectFactory getJpaObjectFactory() {
        return jpaObjectFactory;
    }

    public EntityIdAccessor getEntityIdAccessor() {
        return entityIdAccessor;
    }
}
