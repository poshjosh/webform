package com.looseboxes.webform.repository;

import com.looseboxes.webform.configurers.EntityMapper;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;

/**
 * @author hp
 */
public class MappedEntityRepository<E extends Object> implements EntityRepository<E, Object>{

    private final EntityMapper<Object, E> entityMapper;
    private final EntityRepository<E, Object> entityRepository;

    public MappedEntityRepository(
            EntityMapper<Object, E> entityMapper, 
            EntityRepository<E, Object> entityRepository) {
        this.entityMapper = Objects.requireNonNull(entityMapper);
        this.entityRepository = Objects.requireNonNull(entityRepository);
    }

    @Override
    public Collection<String> getUniqueColumns() {
        return entityRepository.getUniqueColumns();
    }

    @Override
    public Optional<Object> getIdOptional(E entity) {
        entity = this.entityMapper.toEntity(entity);
        return entityRepository.getIdOptional(entity);
    }

    @Override
    public List<E> findAllBy(String key, Object value, int offset, int limit) {
        return entityRepository.findAllBy(key, value, offset, limit);
    }

    @Override
    public E findByIdOrException(Object id) throws EntityNotFoundException {
        return entityRepository.findByIdOrException(id);
    }

    @Override
    public Optional<E> findById(Object id) {
        return entityRepository.findById(id);
    }

    @Override
    public void deleteById(Object id) {
        entityRepository.deleteById(id);
    }

    @Override
    public <S extends E> S save(S entity) {
        entity = (S)this.entityMapper.toEntity(entity);
        return entityRepository.save(entity);
    }
}
