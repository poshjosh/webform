package com.looseboxes.webform.converters;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public interface StringToTemporalConverter<T extends Temporal> extends Converter<String, T> {

    @Override
    T convert(String from);

    StringToTemporalConverter<Instant> instantInstance();

    StringToTemporalConverter<LocalDate> localDateInstance();

    StringToTemporalConverter<LocalDateTime> localDateTimeInstance();

    StringToTemporalConverter<LocalTime> localTimeInstance();

    StringToTemporalConverter<ZonedDateTime> zonedDateTimeInstance();
}
