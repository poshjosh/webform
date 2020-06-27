package com.looseboxes.webform.converters;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import com.looseboxes.webform.entity.EntityRepositoryProvider;

/**
 * @author hp
 */
public class DomainTypeToIdConverter implements Converter<Object, Object>{

    private static final Logger LOG = LoggerFactory.getLogger(DomainTypeToIdConverter.class);
    
    private final EntityRepositoryProvider repoFactory;

    public DomainTypeToIdConverter(EntityRepositoryProvider repoFactory) {
        this.repoFactory = Objects.requireNonNull(repoFactory);
    }

    @Override
    public Object convert(Object source) {
        final Object update;
        if(source == null) {
            update = null;
        }else{
            
            final Class sourceType = source.getClass();
            
            if(repoFactory.isSupported(sourceType)) {
                
                final Object id = repoFactory.forEntity(sourceType)
                        .getIdOptional(source).orElse(null);
                if(id != null) {
                    update = id;
                }else{
                    update = null;
                }
            }else{
                update = null;
            }
        }
        if(update != null) {
            LOG.trace("Converted: {} to: {}", source, update);
        }
        return update == null ? source : update;
    }
}
