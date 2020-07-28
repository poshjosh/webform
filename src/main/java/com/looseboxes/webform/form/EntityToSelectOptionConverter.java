package com.looseboxes.webform.form;

import com.bc.webform.choices.SelectOption;
import com.bc.webform.choices.SelectOptionImpl;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import com.looseboxes.webform.converters.DomainTypePrinter;

/**
 * @author hp
 */
public class EntityToSelectOptionConverter<V> implements BiFunction<Object, Locale, SelectOption<V>>{

    private final DomainTypeToIdConverter domainTypeToIdConverter;
    private final DomainTypePrinter domainObjectPrinter;

    public EntityToSelectOptionConverter(
            DomainTypeToIdConverter domainTypeToIdConverter, 
            DomainTypePrinter domainObjectPrinter) {
        this.domainTypeToIdConverter = Objects.requireNonNull(domainTypeToIdConverter);
        this.domainObjectPrinter = Objects.requireNonNull(domainObjectPrinter);
    }
    
    @Override
    public SelectOption<V> apply(Object source, Locale locale) {
        V id = (V)this.domainTypeToIdConverter.convert(source);
        String text = this.domainObjectPrinter.print(source, locale);
        return new SelectOptionImpl(id, text);
    }
}
