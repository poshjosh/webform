package com.looseboxes.webform.converters;

import com.bc.webform.WebformUtil;
import com.bc.webform.choices.SelectOption;
import java.util.Locale;
import java.util.function.BiFunction;

/**
 * @author hp
 */
public interface EntityToSelectOptionConverter<V> extends 
        BiFunction<Object, Locale, SelectOption<V>>{
    
    @Override
    default SelectOption<V> apply(Object source, Locale locale) {
        return WebformUtil.toSelectOption(convert(source), print(source, locale));
    }
    
    V convert(Object source);
    
    default String print(Object source, Locale locale) {
        return String.valueOf(source);
    }
}
