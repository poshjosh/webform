package com.looseboxes.webform.converters;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;

import com.looseboxes.webform.Errors;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public interface StringToTemporalConverter<T extends Temporal> extends Converter<String, T> {

    @Override
    T convert(String from);

    <TT extends Temporal> TT convert(String from, Class<TT> targetType) throws DateTimeParseException;

    StringToTemporalConverter<T> instance(Class<T> targetType);
}
