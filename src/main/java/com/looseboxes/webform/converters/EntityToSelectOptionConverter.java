package com.looseboxes.webform.converters;

import com.bc.webform.choices.SelectOption;
import com.bc.webform.choices.SelectOptionImpl;
import java.util.Locale;
import java.util.function.BiFunction;

/**
 * @author hp
 */
public interface EntityToSelectOptionConverter<V> extends 
        BiFunction<Object, Locale, SelectOption<V>>{
    
    @Override
    default SelectOption<V> apply(Object source, Locale locale) {
        return new SelectOptionImpl(convert(source), print(source, locale));
    }
    
    V convert(Object source);
    
    default String print(Object source, Locale locale) {
        return String.valueOf(source);
    }
}
