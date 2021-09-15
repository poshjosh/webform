package com.looseboxes.webform.converters;

import com.looseboxes.webform.Errors;

import java.time.*;
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

    private final DateAndTimePatternsSupplier patternSupplier;
    private final ZoneId zoneId;
    private final Class<T> targetType;

    public StringToTemporalConverterImpl(DateAndTimePatternsSupplier patternSupplier, ZoneId zoneId) {
        this(patternSupplier, zoneId, null);
    }
    
    private StringToTemporalConverterImpl(
            DateAndTimePatternsSupplier patternSupplier, ZoneId zoneId, Class<T> targetType) {
        this.patternSupplier = Objects.requireNonNull(patternSupplier);
        this.zoneId = Objects.requireNonNull(zoneId);
        this.targetType = targetType;
    }

    @Override
    public T convert(String from) {
        final T converted;
        if(targetType != null) {
            converted = this.convert(from, targetType);
        }else{
            converted = (T)this.convert(from, new Class[]{LocalDateTime.class, ZonedDateTime.class, Instant.class, LocalDate.class, LocalTime.class});
        }
        LOG.trace("Converted {} to: {}", from, converted);
        return converted;
    }

    private Temporal convert(String text, Class[]types) throws DateTimeParseException{
        Temporal converted = null;
        DateTimeParseException exception = null;
        for(Class type : types) {
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

    @Override
    public <TT extends Temporal> TT convert(String from, Class<TT> targetType) throws DateTimeParseException{
        TT converted = null;
        DateTimeParseException exception = null;
        Set<String> patterns = this.patternSupplier.getPatterns(targetType);
        for(String pattern : patterns) {
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
            try{
                converted = (TT)this.convert(targetType, from, fmt);
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

    private <TT extends Temporal> TT convert(Class<TT> type, String text, DateTimeFormatter fmt) {
        final Temporal result;
        if(LocalDateTime.class.isAssignableFrom(type)) {
            result = LocalDateTime.parse(text, fmt);
        }else if(ZonedDateTime.class.isAssignableFrom(type)) {
            result = ZonedDateTime.parse(text, fmt);
        }else if(Instant.class.isAssignableFrom(type)) {
            result = convertToInstant(text, fmt);
        }else if(LocalDate.class.isAssignableFrom(type)) {
            result = LocalDate.parse(text, fmt);
        }else if(LocalTime.class.isAssignableFrom(type)) {
            result = LocalTime.parse(text, fmt);
        }else{
            throw Errors.unexpectedElement(type, new Class[]{LocalDateTime.class, ZonedDateTime.class, Instant.class, LocalDate.class, LocalTime.class});
        }
        return (TT)result;
    }

    private Instant convertToInstant(String text, DateTimeFormatter fmt) {
        try{
            return Instant.parse(text);
        }catch(DateTimeParseException e) {
            try {
                ZonedDateTime zonedDateTime = this.convert(ZonedDateTime.class, text, fmt);
                return zonedDateTime.toInstant();
            }catch(DateTimeParseException e1) {
                try {
                    LocalDateTime localDateTime = this.convert(LocalDateTime.class, text, fmt);
                    return localDateTime.toInstant(ZoneOffset.UTC);
                }catch(DateTimeParseException e2) {
                    try {
                        LocalTime localTime = this.convert(LocalTime.class, text, fmt);
                        return LocalDate.now().atTime(localTime).toInstant(ZoneOffset.UTC);
                    }catch(DateTimeParseException e3) {
                        e1.addSuppressed(e);
                        e2.addSuppressed(e1);
                        e3.addSuppressed(e2);
                        throw e3;
                    }
                }
            }
        }
    }

    @Override
    public StringToTemporalConverter<T> instance(Class<T> targetType) {
        return new StringToTemporalConverterImpl(this.patternSupplier, zoneId, targetType);
    }
}
