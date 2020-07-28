package com.looseboxes.webform.converters;

import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import java.util.Locale;
import java.util.Objects;
import com.looseboxes.webform.converters.DomainTypePrinter;

/**
 * @author hp
 */
public class EntityToSelectOptionConverterImpl<V> implements EntityToSelectOptionConverter<V>{

    private final DomainTypeToIdConverter domainTypeToIdConverter;
    private final DomainTypePrinter domainObjectPrinter;

    public EntityToSelectOptionConverterImpl(
            DomainTypeToIdConverter domainTypeToIdConverter, 
            DomainTypePrinter domainObjectPrinter) {
        this.domainTypeToIdConverter = Objects.requireNonNull(domainTypeToIdConverter);
        this.domainObjectPrinter = Objects.requireNonNull(domainObjectPrinter);
    }
    
    @Override
    public V convert(Object source) {
        return (V)this.domainTypeToIdConverter.convert(source);
    }
    
    @Override
    public String print(Object source, Locale locale) {
        return this.domainObjectPrinter.print(source, locale);
    }
}
