package com.looseboxes.webform.converters;

import com.looseboxes.webform.Errors;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public class StringToTemporalConverterImpl<T extends Temporal> implements Converter<String, T>, StringToTemporalConverter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(StringToTemporalConverterImpl.class);

    private static enum Type{
        LOCAL_DATETIME, ZONED_DATETIME, INSTANT, LOCAL_DATE, LOCAL_TIME
    }
    
    private final DateAndTimePatternsSupplier patternSupplier;
    private final Set<String> patterns;

    public StringToTemporalConverterImpl(DateAndTimePatternsSupplier patternSupplier) {
        this(patternSupplier, Type.ZONED_DATETIME);
    }
    
    private StringToTemporalConverterImpl(
            DateAndTimePatternsSupplier patternSupplier, Type type) {
        this.patternSupplier = Objects.requireNonNull(patternSupplier);
        this.patterns = this.getPatterns(type);
    }

    @Override
    public T convert(String from) {
        for(String pattern : patterns) {
            final T t = this.convert(from, pattern);
            LOG.trace("Converted {} to {}", from, t);
            return t;
        }
        throw new UnsupportedOperationException("Unable to convert: " + from +
                " to a temporal type using any of: " + patterns);
    }
    
    public T convert(String text, String pattern) {
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        return this.convert(text, fmt);
    }
    
    public T convert(String text, DateTimeFormatter fmt) {
        return (T)ZonedDateTime.parse(text, fmt);
    }

    @Override
    public StringToTemporalConverterImpl<LocalDateTime> localDateTimeInstance() {
        return new StringToTemporalConverterImpl(this.patternSupplier, Type.LOCAL_DATETIME);
    }

    @Override
    public StringToTemporalConverterImpl<ZonedDateTime> zonedDateTimeInstance() {
        return new StringToTemporalConverterImpl(this.patternSupplier, Type.ZONED_DATETIME);
    }

    @Override
    public StringToTemporalConverterImpl<Instant> instantInstance() {
        return new StringToTemporalConverterImpl(this.patternSupplier, Type.INSTANT);
    }

    @Override
    public StringToTemporalConverterImpl<LocalDate> localDateInstance() {
        return new StringToTemporalConverterImpl(this.patternSupplier, Type.LOCAL_DATE);
    }

    @Override
    public StringToTemporalConverterImpl<LocalTime> localTimeInstance() {
        return new StringToTemporalConverterImpl(this.patternSupplier, Type.LOCAL_TIME);
    }

    private Set<String> getPatterns(Type type) {
        final Set<String> patterns;
        switch(type) {
            case LOCAL_DATETIME: 
            case ZONED_DATETIME: 
            case INSTANT: 
                patterns = patternSupplier.getDatetimePatterns();
                break;
                
            case LOCAL_DATE: 
                patterns = patternSupplier.getDatePatterns(); 
                break;
            
            case LOCAL_TIME: 
                patterns = patternSupplier.getTimePatterns(); 
                break;
            default: throw Errors.unexpected(type, (Object[])Type.values());
        }        
        LOG.trace("Type: {}, Patterns: {}", type,patterns);
        return patterns;
    }
}
