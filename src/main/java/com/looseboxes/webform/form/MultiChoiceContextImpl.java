package com.looseboxes.webform.form;

import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.functions.FormInputContext;
import com.bc.webform.functions.MultiChoiceContextForJpaEntity;
import com.bc.webform.functions.TypeTests;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class MultiChoiceContextImpl extends MultiChoiceContextForJpaEntity{
    
    private static final Logger LOG = LoggerFactory.getLogger(MultiChoiceContextImpl.class);
    
    private final EntityRepositoryFactory repoFactory;
    private final PropertySearch propertySearch;
    private final FormInputContext<Object, Field, Object> formInputContext;
    private final DomainObjectPrinter printer;
    private final Locale locale;

    public MultiChoiceContextImpl(
            TypeTests typeTests,
            EntityRepositoryFactory repoFactory, 
            PropertySearch propertySearch,
            FormInputContext<Object, Field, Object> formInputContext,
            DomainObjectPrinter printer, 
            Locale locale) {
        super(typeTests);
        this.repoFactory = Objects.requireNonNull(repoFactory);
        this.propertySearch = Objects.requireNonNull(propertySearch);
        this.formInputContext = Objects.requireNonNull(formInputContext);
        this.printer = Objects.requireNonNull(printer);
        this.locale = Objects.requireNonNull(locale);
    }

    @Override
    public Map getEnumChoices(Class type) {
        if(this.getTypeTests().isEnumType(type)) {
            final Object [] enums = type.getEnumConstants();
            if(enums != null) {
                final Map choices = new HashMap<>(enums.length, 1.0f);
                for(int i = 0; i<enums.length; i++) {
                    choices.put(i, this.printer.print(enums[i], locale));
                }
                return Collections.unmodifiableMap(choices);
            }
        }
        return Collections.EMPTY_MAP;
    }
    
    public PropertySearch getPropertySearch() {
        return propertySearch;
    }

    public DomainObjectPrinter getDomainObjectPrinter() {
        return printer;
    }

    public Locale getLocale() {
        return locale;
    }
}
