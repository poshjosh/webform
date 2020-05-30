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
public class ConverterImpl implements GenericConverter{
    
    private static final Logger LOG = LoggerFactory.getLogger(ConverterImpl.class);
    
    private final Set<ConvertiblePair> supportedTypes;
    
    private final Converter<Object, String> domainTypeToStringConverter;
    
    private final IdToDomainTypeConverterFactory idToDomainTypeConverterFactory;
    
    public ConverterImpl(Set<Class> classes,
            Converter<Object, String> domainTypeToStringConverter,
            IdToDomainTypeConverterFactory idToDomainTypeConverterFactory) {
        final Set<ConvertiblePair> result = new HashSet<>(classes.size() * 2, 1.0f);
        for(Class cls : classes) {
            result.add(new ConvertiblePair(String.class, cls));
            result.add(new ConvertiblePair(cls, String.class));
        }
        this.supportedTypes = Collections.unmodifiableSet(result);
        this.domainTypeToStringConverter = Objects.requireNonNull(domainTypeToStringConverter);
        this.idToDomainTypeConverterFactory = Objects.requireNonNull(idToDomainTypeConverterFactory);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return supportedTypes;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        final Class srcType = sourceType.getType();
        final Class tgtType = targetType.getType();
        LOG.trace("Converting {} to {}", source, tgtType);
        if(String.class.equals(srcType)) {
            return this.idToDomainTypeConverterFactory
                    .getConverter(tgtType).convert(source.toString());
        }else{
            return this.domainTypeToStringConverter.convert(source);
        }
    }
}
