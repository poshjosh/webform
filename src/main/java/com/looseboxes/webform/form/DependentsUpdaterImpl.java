package com.looseboxes.webform.form;

import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.Form;
import com.bc.webform.FormBean;
import com.bc.webform.FormMember;
import com.bc.webform.FormMemberBean;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hp
 */
public class DependentsUpdaterImpl implements DependentsUpdater {
    
    private final EntityRepositoryFactory repoFactory;
    private final DomainObjectPrinter domainObjectPrinter;

    public DependentsUpdaterImpl(
            EntityRepositoryFactory repoFactory,
            DomainObjectPrinter domainObjectPrinter) {
        this.repoFactory = Objects.requireNonNull(repoFactory);
        this.domainObjectPrinter = Objects.requireNonNull(domainObjectPrinter);
    }
    
    @Override
    public Form<Object> update(Form<Object> form, Class memberType, 
            List memberEntities, Locale locale) {
        
        final Map memberChoices = buildChoices(memberType, memberEntities, locale);
        
        return this.update(form, memberType, memberChoices);
    }
    
    public Form<Object> update(Form<Object> form, Class memberType, Map memberChoices) {
        Objects.requireNonNull(memberType);
        Objects.requireNonNull(memberChoices);

        final Function<FormMember, FormMember> mapper = (member) -> {
            final Class fieldType = ((Field)member.getDataSource()).getType();
            if(memberType.equals(fieldType)) {
                return member.building()
                        .apply(new FormMemberBean().multiChoice(true).choices(memberChoices))
                        .build();
            }else{
                return member;
            }
        };

        final List<FormMember> formMembers = form.getMembers();
        final List<FormMember> update = formMembers.stream().map(mapper).collect(Collectors.toList());

        return form.building(Field.class)
                .apply(new FormBean().members(update))
                .build();
    }

    public Map buildChoices(Class memberType, List entities, Locale locale) {
        final Map choices;
        if(entities.isEmpty()) {
            choices = Collections.EMPTY_MAP;
        }else{
            choices = new HashMap(entities.size(), 1.0f);
        }
        final EntityRepository repo = this.repoFactory.forEntity(memberType);
        entities.forEach((entity) -> {
            final Object key = repo.getIdOptional(entity).orElse(null);
            Objects.requireNonNull(key);
            final String val = this.domainObjectPrinter.print(entity, locale);
            Objects.requireNonNull(val);
            choices.put(key, val);
        });
        return choices;
    }
}
