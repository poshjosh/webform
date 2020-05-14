package com.looseboxes.webform.form;

import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.Form;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.store.PropertySearch;
import com.bc.webform.functions.TypeTests;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author hp
 */
public class FormFieldChoicesImpl implements FormFieldChoices {
    
    private final TypeTests typeTests;
    private final EntityRepositoryFactory repoFactory;
    private final PropertySearch propertySearch;
    private final DomainObjectPrinter printer;
    private final Locale locale;

    public FormFieldChoicesImpl(
            TypeTests typeTests,
            EntityRepositoryFactory repoFactory, 
            PropertySearch propertySearch,
            DomainObjectPrinter printer, 
            Locale locale) {
        this.typeTests = Objects.requireNonNull(typeTests);
        this.propertySearch = Objects.requireNonNull(propertySearch);
        this.repoFactory = Objects.requireNonNull(repoFactory);
        this.printer = Objects.requireNonNull(printer);
        this.locale = Objects.requireNonNull(locale);
    }
    
    @Override
    public Map getChoices(Form form, Object source, Field field) {

        final Map output;
        final Object value = this.getFieldValue(form, source, field);
        final Class type = field.getType();
        if(value != null) {
            final Object id = this.repoFactory
                    .forEntity(type).getIdOptional(value).orElse(null);
            return Collections.singletonMap(
                    id, this.printer.print(value, locale));
        }else if(typeTests.isEnumType(type)) {
            output = this.getEnumChoices(type);
        }else if(this.repoFactory.isSupported(type)) {
            final EntityRepository repo = this.repoFactory.forEntity(type);
            output = this.getEntityChoices(repo, 0, this.getMaxItemsInMultichoice());
        }else{
            output = Collections.EMPTY_MAP;
        }
        
        return output;
    }
    
    private int getMaxItemsInMultichoice() {
        final String pname = WebformProperties.MAX_ITEMS_IN_MULTICHOICE;
        final String pvalue = this.propertySearch.find(pname)
                .orElseThrow(() -> Errors.propertyValueNotFound(pname));
        return Integer.parseInt(pvalue);
    }

    @Override
    public boolean hasValues(Form form, Object source, Field field) {
        final boolean output;
        final Object fieldValue = this.getFieldValue(form, source, field);
        final Class fieldType = field.getType();
        if(fieldValue != null) {
            return true;
        }else if(typeTests.isEnumType(fieldType)) {
            output = fieldType.getEnumConstants().length > 0;
        }else if(this.repoFactory.isSupported(fieldType)) {
            final EntityRepository repo = this.repoFactory.forEntity(fieldType);
            output = this.hasDatabaseRecords(repo);
        }else{
            output = false;
        }
        return output;
    }

    public boolean hasDatabaseRecords(EntityRepository repo) {
        return ! this.getDatabaseRecords(repo, 0, 1).isEmpty();
    }

    public Map getEntityChoices(EntityRepository repo, int offset, int limit) {
        final List list = this.getDatabaseRecords(repo, offset, limit);
        final Map output;
        if(list == null || list.isEmpty()) {
            output = Collections.EMPTY_MAP;
        }else{
            output = Collections.unmodifiableMap(this.toMap(repo, list));
        }
        return output;
    }

    public Map getEnumChoices(Class type) {
        if(this.typeTests.isEnumType(type)) {
            final Object [] enums = type.getEnumConstants();
            if(enums != null) {
                final Map choices = new HashMap<>(enums.length, 1.0f);
                for(int i = 0; i<enums.length; i++) {
                    choices.put((i + 1), this.printer.print(enums[i], locale));
                }
                return Collections.unmodifiableMap(choices);
            }
        }
        return Collections.EMPTY_MAP;
    }
    

    public List getDatabaseRecords(EntityRepository repo, int offset, int limit) {
        return repo.findAll(offset, limit);
    }
    
    public Map toMap(EntityRepository repo, List list) {
        final Map output = new HashMap(list.size(), 1.0f);
        list.forEach((e) -> {
            repo.getIdOptional(e).ifPresent((id) -> {
                output.put(id, this.printer.print(e, locale));
            });
        });
        return output;
    }

    public Object getFieldValue(Form form, Object object, Field field) {
        final Object fieldValue;
        try{
            if(object == null) {
                fieldValue = null;
            }else{
                final boolean prev = field.isAccessible();
                try{
                    field.setAccessible(true);
                    fieldValue = field.get(object);
                }finally{
                    field.setAccessible(prev);
                }
            }
        }catch(IllegalAccessException | IllegalArgumentException e) {
            throw Errors.objectFieldMismatch(object, field, e);
        }
        return fieldValue;
    }
}
