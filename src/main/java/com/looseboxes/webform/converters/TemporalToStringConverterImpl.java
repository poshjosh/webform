package com.looseboxes.webform.converters;

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

/**
 * @author hp
 */
public class TemporalToStringConverterImpl implements TemporalToStringConverter{
    
    private static final Logger LOG = LoggerFactory
            .getLogger(TemporalToStringConverterImpl.class);

    private final DateAndTimePatternsSupplier patternSupplier;

    public TemporalToStringConverterImpl(DateAndTimePatternsSupplier patternSupplier) {
        this.patternSupplier = Objects.requireNonNull(patternSupplier);
    }
    
    @Override
    public String convert(Temporal t) {
        final Set<String> patterns = this.getPatterns(t);
        for(String pattern : patterns) {
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
            try{
                final String result = fmt.format(t);
                LOG.trace("Converted {} to {}", t, result);
                return result;
            }catch(RuntimeException ignored) {}
        }
        return t.toString();
    }
    
    public Set<String> getPatterns(Temporal t) {
        final Set<String> patterns;
        if(t instanceof LocalDateTime) {
            patterns = this.patternSupplier.getDatetimePatterns();
        }else if(t instanceof ZonedDateTime) {
            patterns = this.patternSupplier.getDatetimePatterns();
        }else if(t instanceof Instant) {
            patterns = this.patternSupplier.getDatetimePatterns();
        }else if(t instanceof LocalDate) {
            patterns = this.patternSupplier.getDatePatterns();
        }else if(t instanceof LocalTime) {
            patterns = this.patternSupplier.getTimePatterns();
        }else{
            throw new UnsupportedOperationException("Temporal type not supported: " 
                    + t.getClass() + ", instance: " + t);
        }
        return patterns;
    }
}
