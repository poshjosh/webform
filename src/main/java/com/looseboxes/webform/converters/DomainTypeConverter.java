package com.looseboxes.webform.converters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * @author hp
 */
public class DomainTypeConverter implements GenericConverter{
    
    private static final Logger LOG = LoggerFactory.getLogger(DomainTypeConverter.class);
    
    private final Set<Class> supportedTypes;
    
    private final Set<ConvertiblePair> convertibleTypes;
    
    private final Converter<Object, String> domainTypeToStringConverter;
    
    private final IdToDomainTypeConverterFactory idToDomainTypeConverterFactory;
    
    public DomainTypeConverter(Set<Class> supportedTypes,
            Converter<Object, String> domainTypeToStringConverter,
            IdToDomainTypeConverterFactory idToDomainTypeConverterFactory) {
        this.supportedTypes = Collections.unmodifiableSet(supportedTypes);
        this.convertibleTypes = Collections
                .unmodifiableSet(this.toConvertiblePairs(supportedTypes));
        this.domainTypeToStringConverter = Objects.requireNonNull(domainTypeToStringConverter);
        this.idToDomainTypeConverterFactory = Objects.requireNonNull(idToDomainTypeConverterFactory);
    }
    
    private Set<ConvertiblePair> toConvertiblePairs(Set<Class> supportedTypes) {
        final Set<ConvertiblePair> result = new HashSet<>(supportedTypes.size() * 2, 1.0f);
        for(Class cls : supportedTypes) {
            result.add(new ConvertiblePair(String.class, cls));
            result.add(new ConvertiblePair(cls, String.class));
        }
        return result;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return convertibleTypes;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        try{
            final Class srcType = sourceType.getType();
            final Class tgtType = targetType.getType();
            LOG.trace("Converting {} to {}", source, tgtType);
            if(this.supportedTypes.contains(srcType)) {
                return this.domainTypeToStringConverter.convert(source);
            }else{
                return this.idToDomainTypeConverterFactory
                        .getConverter(tgtType).convert(source.toString());
            }
        }catch(RuntimeException e) {
            LOG.warn("Unexpected exception", e);
            throw e;
        }
    }
}
