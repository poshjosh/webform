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
    
    private final IdStringToDomainTypeConverterFactory idToDomainTypeConverterFactory;
    
    public DomainTypeConverter(Set<Class> supportedTypes,
            Converter<Object, String> domainTypeToStringConverter,
            IdStringToDomainTypeConverterFactory idToDomainTypeConverterFactory) {
        this.supportedTypes = Collections.unmodifiableSet(supportedTypes);
        LOG.debug("Supported types: {}", supportedTypes);
        this.convertibleTypes = Collections
                .unmodifiableSet(this.toConvertiblePairs(supportedTypes));
        this.domainTypeToStringConverter = Objects.requireNonNull(domainTypeToStringConverter);
        this.idToDomainTypeConverterFactory = Objects.requireNonNull(idToDomainTypeConverterFactory);
    }
    
    private Set<ConvertiblePair> toConvertiblePairs(Set<Class> supportedTypes) {
        final Set<ConvertiblePair> result = new HashSet<>(supportedTypes.size() * 2, 1.0f);
        for(Class cls : supportedTypes) {
            this.addConvertiblePairs(result, cls);
        }
        return result;
    }

    private Set<ConvertiblePair> addConvertiblePairs(Set<ConvertiblePair> addTo, Class cls) {
        addTo.add(new ConvertiblePair(String.class, cls));
        addTo.add(new ConvertiblePair(cls, String.class));
        return addTo;
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
//            LOG.trace("Converting {} to {}", sourceType, targetType);
            final Object target;
            if(this.supportedTypes.contains(srcType)) {
                target = this.domainTypeToStringConverter.convert(source);
            }else{
                target = this.idToDomainTypeConverterFactory
                        .getConverter(tgtType).convert(source.toString());
            }
            LOG.trace("Converted {} to: {}", source, target);
            return target;
        }catch(RuntimeException e) {
            LOG.warn("Unexpected exception", e);
            throw e;
        }
    }

    
    public boolean isConvertible(Class srcType, Class tgtType) {
// DIDN'T WORK        
//        ConvertiblePair cp = new ConvertiblePair(srcType, tgtType);
//        final boolean supported = this.convertibleTypes.contains(cp);
//        LOG.debug("Supported: {}, candidate: {}, collection: {}",
//                supported, cp, this.convertibleTypes);
// DIDN'T WORK
//        final boolean supported = 
//                (supportedTypes.contains(srcType) && String.class.equals(tgtType))
//                || (supportedTypes.contains(tgtType) && String.class.equals(srcType));
        final boolean supported = 
                (containsName(srcType.getName()) && String.class.equals(tgtType))
                || (containsName(tgtType.getName()) && String.class.equals(srcType));
            LOG.trace("Supported: {}. {} -> {}", supported, srcType.getName(), tgtType.getName());
        return supported;
    }
    
    private boolean containsName(String typeName) {
        for(Class cls : supportedTypes) {
            if(cls.getName().equals(typeName)) {
                return true;
            }
        }
        return false;
//        return supportedTypes.stream().filter((cls) -> cls.getName().equals(typeName)).findAny().isPresent();
    }
}
