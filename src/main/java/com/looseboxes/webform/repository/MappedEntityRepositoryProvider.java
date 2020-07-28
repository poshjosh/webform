package com.looseboxes.webform.repository;

import com.looseboxes.webform.mappers.EntityMapper;
import com.looseboxes.webform.mappers.EntityMapperService;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManagerFactory;

/**
 * @author hp
 */
public class MappedEntityRepositoryProvider implements EntityRepositoryProvider{
    
    private final EntityMapperService entityMapperService;
    private final EntityRepositoryProvider entityRepositoryProvider;

    public MappedEntityRepositoryProvider(
            EntityMapperService entityMapperService, 
            EntityRepositoryProvider entityRepositoryProvider) {
        this.entityMapperService = Objects.requireNonNull(entityMapperService);
        this.entityRepositoryProvider = Objects.requireNonNull(entityRepositoryProvider);
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return entityRepositoryProvider.getEntityManagerFactory();
    }

    @Override
    public boolean isSupported(Class entityType) {

        entityType = this.mapToActualEntityTypeIfNeed(entityType);

        return entityRepositoryProvider.isSupported(entityType);
    }

    @Override
    public <E> EntityRepository<E, Object> forEntity(Class<E> entityType) {
        
        entityType = this.mapToActualEntityTypeIfNeed(entityType);
        
        Optional<EntityMapper<Object, E>> mapperOptional = 
                entityMapperService.getMapperForEntity(entityType);
        
        EntityRepository<E, Object> entityRepository = 
                entityRepositoryProvider.forEntity(entityType);
        
        if(mapperOptional.isPresent()) {
            
            entityRepository = new MappedEntityRepository(
                    mapperOptional.get(), entityRepository);
        }
        
        return entityRepository;
    }
    
    private Class mapToActualEntityTypeIfNeed(Class entityType) {
        final Class result = (Class)entityMapperService
                .getEntityType(entityType).orElse(entityType);
        return result;
    }
}
