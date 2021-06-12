package com.looseboxes.webform.converters;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
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
public class TemporalConverter implements GenericConverter{
    
    private static final Logger LOG = LoggerFactory.getLogger(TemporalConverter.class);
    
    private final Converter<Temporal, String> temporalToStringConverter;
    
    private final Converter<String, Temporal> stringToTemporalConverter;
    
    private final Set<ConvertiblePair> convertibleTypes;

    public TemporalConverter(
            Converter<Temporal, String> temporalToStringConverter, 
            Converter<String, Temporal> stringToTemporalConverter) {
        this.temporalToStringConverter = Objects.requireNonNull(temporalToStringConverter);
        this.stringToTemporalConverter = Objects.requireNonNull(stringToTemporalConverter);
        this.convertibleTypes = this.initConvertibleTypes();
    }

    private Set<ConvertiblePair> initConvertibleTypes() {
        final Set<ConvertiblePair> result = new HashSet<>(12, 1.0f);
        this.addConvertiblePairs(result, Instant.class);
        this.addConvertiblePairs(result, LocalDate.class);
        this.addConvertiblePairs(result, LocalDateTime.class);
        this.addConvertiblePairs(result, LocalTime.class);
        this.addConvertiblePairs(result, ZonedDateTime.class);
        return result;
    }
    
    private Set<ConvertiblePair> addConvertiblePairs(Set<ConvertiblePair> addTo, Class cls) {
        addTo.add(new ConvertiblePair(String.class, cls));
        addTo.add(new ConvertiblePair(cls, String.class));
        return addTo;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return this.convertibleTypes;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        try{
            final Class srcType = sourceType.getType();
            final Class tgtType = targetType.getType();
//            LOG.trace("Converting {} to {}", sourceType, targetType);
            final Object target;
            if(String.class.equals(srcType)) {
                target = this.stringToTemporalConverter.convert(source.toString());
            }else if(Temporal.class.isAssignableFrom(srcType)){
                target = this.temporalToStringConverter.convert((Temporal)source);
            }else{
                throw new IllegalArgumentException("Unexpected source type: " + srcType + 
                        ". Cannot convert specified type to/from " + java.time.temporal.Temporal.class.getName());
            }
            LOG.trace("Converted {} to: {}", source, target);
            return target;
        }catch(RuntimeException e) {
            LOG.warn("Unexpected exception", e);
            throw e;
        }
    }
}
