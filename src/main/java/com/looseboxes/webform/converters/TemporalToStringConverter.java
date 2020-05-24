package com.looseboxes.webform.converters;

import java.time.temporal.Temporal;

import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public interface TemporalToStringConverter extends Converter<Temporal, String> {
    
}
