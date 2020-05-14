package com.looseboxes.webform.converters;

import com.looseboxes.webform.Errors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class DateToStringConverterImpl implements DateToStringConverter {

    private static final Logger LOG = LoggerFactory.getLogger(DateToStringConverter.class);
    
    private static enum Type{
        DATE, TIME, DATETIME
    }
    
    private final SimpleDateFormat dateFormat;
    private final DateAndTimePatternsSupplier patternSupplier;

    public DateToStringConverterImpl(DateAndTimePatternsSupplier patternSupplier) {
        this(patternSupplier, Type.DATETIME);
    }
    
    private DateToStringConverterImpl(
            DateAndTimePatternsSupplier patternSupplier, Type type) {
        this.patternSupplier = Objects.requireNonNull(patternSupplier);
        final String datePattern = this.getFirstPattern(type, "yyyy-MM-ddTHH:mm:ss");
        this.dateFormat = new SimpleDateFormat(datePattern);
//        this.dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    @Override
    public String convert(Date from) {
        final String output = dateFormat.format(from);
        LOG.trace("Converted: {}, to: {}, using: {}", 
                from, 
                output, 
                dateFormat.toPattern());
        return output;
    }
    
    @Override
    public DateToStringConverter dateInstance() {
        return new DateToStringConverterImpl(this.patternSupplier, Type.DATE);
    }

    @Override
    public DateToStringConverter timeInstance() {
        return new DateToStringConverterImpl(this.patternSupplier, Type.TIME);
    }

    @Override
    public DateToStringConverter datetTimeInstance() {
        return new DateToStringConverterImpl(this.patternSupplier, Type.DATETIME);
    }

    private String getFirstPattern(Type type, String resultIfNone) {
        final Set<String> patterns;
        switch(type) {
            case DATE: patterns = patternSupplier.getDatePatterns(); break;
            case TIME: patterns = patternSupplier.getTimePatterns(); break;
            case DATETIME: patterns = patternSupplier.getDatetimePatterns(); break;
            default: throw Errors.unexpected("Type", type, (Object[])Type.values());
        }        
        final String first = patterns.stream().findFirst().orElse(null);
        LOG.trace("Type: {}, first pattern: {}, all patterns: {}", 
                type, first, patterns);
        return first == null ? resultIfNone : first;
    }
}
