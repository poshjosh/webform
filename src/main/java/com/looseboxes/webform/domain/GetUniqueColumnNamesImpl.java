package com.looseboxes.webform.domain;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.jpa.dao.functions.GetTableName;
import com.bc.jpa.spring.util.JpaUtil;
import com.looseboxes.webform.mappers.EntityMapperService;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class GetUniqueColumnNamesImpl implements GetUniqueColumnNames {
    
    private static final Logger LOG = LoggerFactory.getLogger(GetUniqueColumnNamesImpl.class);

    private final EntityMapperService entityMapperService;
    private final MetaDataAccess metaDataAccess;
    
    public GetUniqueColumnNamesImpl(EntityMapperService entityMapperService, MetaDataAccess metaDataAccess) {
        this.entityMapperService = Objects.requireNonNull(entityMapperService);
        this.metaDataAccess = Objects.requireNonNull(metaDataAccess);
    }
    
    @Override
    public Collection<String> apply(Class entityType) {
        
        entityType = this.mapToActualEntityTypeIfNeed(entityType);
        
        entityType = JpaUtil.deduceActualDomainType(entityType);

        final String tableName = new GetTableName(metaDataAccess).apply(entityType);

        final List<String> indexNames = metaDataAccess
                .fetchStringIndexInfo(tableName, true, MetaDataAccess.INDEX_NAME);

        final Collection<String> result;
        
        if(indexNames == null || indexNames.isEmpty()) {
            result = Collections.EMPTY_SET;
        }else{
            final List<String> columnNames = metaDataAccess
                    .fetchStringMetaData(tableName, MetaDataAccess.COLUMN_NAME);
            result = indexNames.stream()
                    .filter((name) -> columnNames.contains(name))
                    .collect(Collectors.toSet());
        }

        LOG.trace("Entity: {}, unique columns: {}", entityType.getName(), result);

        return result;
    }

    private Class mapToActualEntityTypeIfNeed(Class entityType) {
        final Class result = (Class)entityMapperService
                .getEntityType(entityType).orElse(entityType);
        return result;
    }
    
    public MetaDataAccess getMetaDataAccess() {
        return metaDataAccess;
    }
}
