package com.looseboxes.webform.converters;

import com.bc.webform.choices.SelectOption;
import com.bc.webform.choices.SelectOptionImpl;
import java.util.Locale;
import java.util.function.BiFunction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Printer;

/**
 * @author hp
 */
public interface EntityToSelectOptionConverter<V> extends 
        BiFunction<Object, Locale, SelectOption<V>>,
        Converter<Object, V>,
        Printer<Object>{

    @Override
    default SelectOption<V> apply(Object source, Locale locale) {
        return new SelectOptionImpl(convert(source), print(source, locale));
    }
    
    @Override
    V convert(Object source);
    
    @Override
    default String print(Object source, Locale locale) {
        return String.valueOf(source);
    }
}
