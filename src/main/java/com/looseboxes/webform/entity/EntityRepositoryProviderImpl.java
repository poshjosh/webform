package com.looseboxes.webform.entity;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import java.util.Objects;
import javax.persistence.EntityManagerFactory;

/**
 * @author hp
 */
public class EntityRepositoryProviderImpl implements EntityRepositoryProvider{

    private final EntityRepositoryFactory entityRepositoryFactory;
    
    private final MetaDataAccess metaDataAccess;
    
    public EntityRepositoryProviderImpl(
            EntityRepositoryFactory repoFactory, MetaDataAccess mda) {
        this.entityRepositoryFactory = Objects.requireNonNull(repoFactory);
        this.metaDataAccess = Objects.requireNonNull(mda);
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return entityRepositoryFactory.getEntityManagerFactory();
    }

    @Override
    public boolean isSupported(Class entityType) {
        return entityRepositoryFactory.isSupported(entityType);
    }

    @Override
    public <E> EntityRepository<E> forEntity(Class<E> entityType) {
        return new EntityRepositoryImpl(entityRepositoryFactory.forEntity(entityType), this.metaDataAccess);
    }

    public EntityRepositoryFactory getEntityRepositoryFactory() {
        return entityRepositoryFactory;
    }

    public MetaDataAccess getMetaDataAccess() {
        return metaDataAccess;
    }
}
