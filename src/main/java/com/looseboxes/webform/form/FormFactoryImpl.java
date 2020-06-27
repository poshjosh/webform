package com.looseboxes.webform.form;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.Form;
import com.bc.webform.FormBuilder;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bc.webform.FormMember;
import java.lang.reflect.Field;

/**
 * @author hp
 */
public class FormFactoryImpl implements FormFactory{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormFactoryImpl.class);
    
    private final TypeFromNameResolver typeFromNameResolver;

    private final FormBuilder<Object, Field, Object> formBuilder;
    
    public FormFactoryImpl(
            TypeFromNameResolver typeFromNameResolver, FormBuilder formBuilder) {
        this.formBuilder = Objects.requireNonNull(formBuilder);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
    }
    
    @Override
    public Form newForm(Form parent, String id, String name) {
        return this.newForm(parent, id, name, typeFromNameResolver
                .newInstanceOptional(name).orElseThrow(
                        () -> new InvalidRouteException("Not found")));
    }
    
    @Override
    public Form newForm(Form parent, String id, String name, Object domainObject) {
    
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        
        final com.bc.webform.Form form = this.formBuilder
                .applyDefaults(name)
                .id(id)
                .parent(parent)
                .dataSource(domainObject)
                .build();

        this.logFormFields(form);

        return form;
    }
    
    private void logFormFields(Form form) {
        
        if(LOG.isDebugEnabled()) {
            
            final Function<FormMember, String> mapper = (ff) -> {
                final Object value = ff.getValue();
                final Map choices = ff.getChoices();
                return ff.getName() + '=' + 
                        (choices==null||choices.isEmpty() ? value : 
                        (String.valueOf(value) + ", " + choices.size() + " choice(s)"));
            };

            LOG.debug("Form fields:\n{}", 
                    form.getMembers().stream()
                            .map(mapper)
                            .collect(Collectors.joining("\n")));
        }
    }
}
