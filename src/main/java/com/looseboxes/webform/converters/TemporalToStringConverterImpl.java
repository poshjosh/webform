package com.looseboxes.webform.converters;

import java.time.*;
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
    private final ZoneId zoneId;

    public TemporalToStringConverterImpl(DateAndTimePatternsSupplier patternSupplier, ZoneId zoneId) {
        this.patternSupplier = Objects.requireNonNull(patternSupplier);
        this.zoneId = Objects.requireNonNull(zoneId);
    }
    
    @Override
    public String convert(Temporal t) {
        final Set<String> patterns = patternSupplier.getPatterns(t.getClass());
        for(String pattern : patterns) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
            try{
                final String result = fmt.format(t);
                LOG.trace("Converted {} to {}", t, result);
                return result;
            }catch(RuntimeException ignored) {}
        }
        return t.toString();
    }
}
