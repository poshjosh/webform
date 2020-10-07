package com.looseboxes.webform.converters;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import com.looseboxes.webform.repository.EntityRepositoryProvider;

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

            if(sourceType.isEnum()) {
                update = ((Enum)source).ordinal();
            }else if(repoFactory.isSupported(sourceType)) {
                
                final Object id = repoFactory.getIdOptional(source).orElse(null);
                
                update = id == null ? null : id;
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
