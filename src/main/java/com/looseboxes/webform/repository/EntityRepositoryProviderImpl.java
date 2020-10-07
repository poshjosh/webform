package com.looseboxes.webform.repository;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.functions.GetTableName;
import com.bc.jpa.spring.EntityIdAccessor;
import com.bc.jpa.spring.util.JpaUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
//import com.bc.jpa.spring.repository.EntityRepositoryImpl;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class EntityRepositoryProviderImpl implements EntityRepositoryProvider{

    private final Logger log = LoggerFactory.getLogger(EntityRepositoryProviderImpl.class);
    
    private final JpaObjectFactory jpaObjectFactory;
    
    private final Predicate<Class> domainTypeTest;
    
    private final MetaDataAccess metaDataAccess;
    
    private final EntityIdAccessor entityIdAccessor;
    
    public EntityRepositoryProviderImpl(
            JpaObjectFactory jpaObjectFactory, Predicate<Class> domainTypeTest,
            MetaDataAccess metaDataAccess, EntityIdAccessor entityIdAccessor) {
        this.jpaObjectFactory = Objects.requireNonNull(jpaObjectFactory);
        this.domainTypeTest = Objects.requireNonNull(domainTypeTest);
        this.metaDataAccess = Objects.requireNonNull(metaDataAccess);
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
    
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return this.jpaObjectFactory.getEntityManagerFactory();
    }
    
    private Collection<String> _uniqueColumns;
    
    @Override
    public Collection<String> getUniqueColumns(Class entityType) {
        
        if(_uniqueColumns == null) {
        
            entityType = JpaUtil.deduceActualDomainType(entityType);
            
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
            log.debug("Entity: {}, unique columns: {}", entityType.getName(), _uniqueColumns);
        }

        log.trace("Entity: {}, unique columns: {}", entityType.getName(), _uniqueColumns);

        return _uniqueColumns;
    }
    
    public Predicate<Class> getDomainTypeTest() {
        return domainTypeTest;
    }
}
