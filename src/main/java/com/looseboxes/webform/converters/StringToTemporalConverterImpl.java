package com.looseboxes.webform.converters;

import com.looseboxes.webform.Errors;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private final Type type;

    public StringToTemporalConverterImpl(DateAndTimePatternsSupplier patternSupplier) {
        this(patternSupplier, null);
    }
    
    private StringToTemporalConverterImpl(
            DateAndTimePatternsSupplier patternSupplier, Type type) {
        this.patternSupplier = Objects.requireNonNull(patternSupplier);
        this.type = type;
    }

    @Override
    public T convert(String from) {
        final T converted;
        if(type != null) {
            converted = this.convert(from, type);
        }else{
            converted = this.convert(from, Type.values());
        }
        LOG.trace("Converted {} to: {}", from, converted);
        return converted;
    }

    private T convert(String text, Type[]types) throws DateTimeParseException{
        T converted = null;
        DateTimeParseException exception = null;
        for(Type type : types) {
            try{
                converted = this.convert(text, type);
                break;
            }catch(DateTimeParseException e) {
                if(exception == null) {
                    exception = e;
                }else{
                    exception.addSuppressed(e);
                }
            }
        }
        if(converted == null && exception != null) {
            throw exception;
        }
        return converted;
    }
    
    private T convert(String text, Type type) throws DateTimeParseException{
        T converted = null;
        DateTimeParseException exception = null;
        Set<String> patterns = this.getPatterns(type);
        for(String pattern : patterns) {
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
            try{
                converted = this.convert(type, text, fmt);
                break;
            }catch(DateTimeParseException e) {
                if(exception == null) {
                    exception = e;
                }else{
                    exception.addSuppressed(e);
                }
            }
        }
        if(converted == null && exception != null) {
            throw exception;
        }
        return converted;
    }
    
    private T convert(Type type, String text, DateTimeFormatter fmt) {
        Temporal result;
        switch(type) {
            case LOCAL_DATETIME: 
                result = LocalDateTime.parse(text, fmt); break;
            case ZONED_DATETIME: 
                result = ZonedDateTime.parse(text, fmt); break;
            case INSTANT: 
                result = Instant.parse(text); break;
            case LOCAL_DATE: 
                result = LocalDate.parse(text, fmt); break;
            case LOCAL_TIME: 
                result = LocalTime.parse(text, fmt); break;
            default: 
                throw Errors.unexpectedElement(type, Type.values());
        }        
        return (T)result;
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
            default: 
                throw Errors.unexpectedElement(type, Type.values());
        }        
        LOG.trace("Type: {}, Patterns: {}", type,patterns);
        return patterns;
    }
}
