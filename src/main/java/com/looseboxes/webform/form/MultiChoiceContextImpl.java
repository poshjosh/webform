package com.looseboxes.webform.form;

import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.functions.FormInputContext;
import com.bc.webform.functions.MultiChoiceContextForJpaEntity;
import com.bc.webform.functions.TypeTests;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    public boolean isMultiChoice(Object source, Field field) {
        final Class fieldType = field.getType();
        final TypeTests typeTests = this.getTypeTests();
        final boolean output;
        if(typeTests.isDomainType(fieldType) && this.hasValues(source, field)) {
            output = true;
        }else{
            output = super.isMultiChoice(source, field);
        }
        LOG.trace("Multichoice: {}, field: {}", output, field);
        return output;
    }

    public boolean hasValues(Object source, Field field) {
        final Object fieldValue = this.formInputContext.getValue(source, field);
        final boolean hasValues = fieldValue != null || this.hasDatabaseRecords(source, field);
        LOG.trace("Has values: {}, {}.{}", hasValues, 
                field.getDeclaringClass().getSimpleName(), field.getName());
        return hasValues;
    }

    @Override
    public Map getChoices(Object source, Field field) {
        
        Map output = null;
        
        final Class fieldType = field.getType();

        final TypeTests typeTests = this.getTypeTests();

        if(typeTests.isEnumType(fieldType) || typeTests.isDomainType(fieldType)) {
            
            LOG.trace("getChoices(..) Source: {}, field: {}",
                    (source==null?null:source.getClass().getName()), field);

            final Class type = field.getType();
            if(typeTests.isEnumType(type)) {
                output = this.getEnumChoices(type);
            }else if(this.repoFactory.isSupported(type)) {
                output = this.getChoicesFromDatabase(type, 0, this.getMaxItemsInMultichoice());
            }else{
                output = Collections.EMPTY_MAP;
            }
        }
        
        if(output == null || output.isEmpty()) {
            
            output = super.getChoices(source, field);
        }
        
        LOG.trace("Choices: {}, field: {}.{}", output, 
                field.getDeclaringClass().getName(), field.getName());
        
        return output == null ? Collections.EMPTY_MAP : output;
    }

    private int getMaxItemsInMultichoice() {
        final String pname = WebformProperties.MAX_ITEMS_IN_MULTICHOICE;
        final String pvalue = this.propertySearch.find(pname)
                .orElseThrow(() -> Errors.propertyValueNotFound(pname));
        return Integer.parseInt(pvalue);
    }

    public boolean hasDatabaseRecords(Object source, Field field) {

        LOG.trace("hasValues(...) Source: {}, field: {}", 
                (source==null?null:source.getClass().getName()), field);
        
        final boolean output;
        final Class fieldType = field.getType();
        if(getTypeTests().isEnumType(fieldType)) {
            output = fieldType.getEnumConstants().length > 0;
        }else if(this.repoFactory.isSupported(fieldType)) {
            output = this.hasDatabaseRecords(fieldType);
        }else{
            output = false;
        }
        return output;
    }

    public boolean hasDatabaseRecords(Class entityType) {
        final EntityRepository repo = this.repoFactory.forEntity(entityType);
        return ! this.getDatabaseRecords(repo, 0, 1).isEmpty();
    }

    public Map getChoicesFromDatabase(Class type, int offset, int limit) {
        final EntityRepository repo = this.repoFactory.forEntity(type);
        final List list = this.getDatabaseRecords(repo, offset, limit);
        final Map output;
        if(list == null || list.isEmpty()) {
            output = Collections.EMPTY_MAP;
        }else{
            output = Collections.unmodifiableMap(this.toMap(repo, list));
        }
        return output;
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

    public EntityRepositoryFactory getRepositoryFactory() {
        return repoFactory;
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
