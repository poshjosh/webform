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
    
    private final DateAndTimePatternsSupplier patternSupplier;
    private final SimpleDateFormat dateFormat;
    private final Set<String> patterns;

    public DateToStringConverterImpl(DateAndTimePatternsSupplier patternSupplier) {
        this(patternSupplier, Type.DATETIME);
    }
    
    private DateToStringConverterImpl(
            DateAndTimePatternsSupplier patternSupplier, Type type) {
        this.patternSupplier = Objects.requireNonNull(patternSupplier);
        this.dateFormat = new SimpleDateFormat();
        this.patterns = this.getPatterns(type);
    }

    @Override
    public String convert(Date from) {
        for(String pattern : patterns) {
            dateFormat.applyPattern(pattern);
            final String output = dateFormat.format(from);
            LOG.trace("Converted: {}, to: {}, using: {}", 
                    from, 
                    output, 
                    dateFormat.toPattern());
            return output;
        }
        return from.toString();
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

    private Set<String> getPatterns(Type type) {
        final Set<String> patterns;
        switch(type) {
            case DATE: patterns = patternSupplier.getDatePatterns(); break;
            case TIME: patterns = patternSupplier.getTimePatterns(); break;
            case DATETIME: patterns = patternSupplier.getDatetimePatterns(); break;
            default: throw Errors.unexpected(type, (Object[])Type.values());
        }        
        LOG.trace("Type: {}, Patterns: {}", type,patterns);
        return patterns;
    }
}
