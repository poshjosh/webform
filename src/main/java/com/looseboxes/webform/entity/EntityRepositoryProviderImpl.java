package com.looseboxes.webform.entity;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import java.util.Objects;
import javax.persistence.EntityManagerFactory;

/**
 * @author hp
 */
public class EntityRepositoryProviderImpl implements EntityRepositoryProvider{

    private final EntityRepositoryFactory repoFactory;
    
    private final MetaDataAccess metaDataAccess;
    
    public EntityRepositoryProviderImpl(
            EntityRepositoryFactory repoFactory, MetaDataAccess mda) {
        this.repoFactory = Objects.requireNonNull(repoFactory);
        this.metaDataAccess = Objects.requireNonNull(mda);
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return repoFactory.getEntityManagerFactory();
    }

    @Override
    public boolean isSupported(Class entityType) {
        return repoFactory.isSupported(entityType);
    }

    @Override
    public <E> EntityRepository<E> forEntity(Class<E> entityType) {
        return new EntityRepositoryImpl(repoFactory.forEntity(entityType), this.metaDataAccess);
    }
}
