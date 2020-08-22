package com.looseboxes.webform.repository;

import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.spring.util.JpaUtil;
//import com.bc.jpa.spring.repository.EntityRepositoryImpl;
import java.util.Objects;
import java.util.function.Predicate;
import javax.persistence.EntityManagerFactory;

/**
 * @author hp
 */
public class EntityRepositoryProviderImpl implements EntityRepositoryProvider{

    private final JpaObjectFactory jpaObjectFactory;
    
    private final Predicate<Class> domainTypeTest;
    
    public EntityRepositoryProviderImpl
        (JpaObjectFactory jpaObjectFactory, Predicate<Class> domainTypeTest) {
        this.jpaObjectFactory = Objects.requireNonNull(jpaObjectFactory);
        this.domainTypeTest = Objects.requireNonNull(domainTypeTest);
    }
    
    @Override
    public boolean isSupported(Class entityType) {
        entityType = JpaUtil.deduceActualDomainType(entityType);
        return domainTypeTest.test(entityType);
    }
    
    @Override
    public <E> EntityRepository<E, Object> forEntity(Class<E> entityType) {
        entityType = JpaUtil.deduceActualDomainType(entityType);
        return new GenericRepository(entityType, this.jpaObjectFactory);
    }
    
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return this.jpaObjectFactory.getEntityManagerFactory();
    }

    public Predicate<Class> getDomainTypeTest() {
        return domainTypeTest;
    }
}
