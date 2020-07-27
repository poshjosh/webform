package com.looseboxes.webform.form;

import com.bc.webform.choices.SelectOption;
import com.bc.webform.form.Form;
import com.bc.webform.form.member.FormMember;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class FormFactoryImpl implements FormFactory{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormFactoryImpl.class);
    
    private final FormBuilderProvider formBuilderProvider;

    public FormFactoryImpl(FormBuilderProvider formBuilderProvider) {
        this.formBuilderProvider = Objects.requireNonNull(formBuilderProvider);
    }
    
    @Override
    public <T> Form<T> newForm(Form<T> parentForm, String id, String name, T domainObject) {
        
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(domainObject);
        
        final Form form = this.formBuilderProvider.get()
                .applyDefaults(name)
                .id(id)
                .parent(parentForm)
                .dataSource(domainObject)
                .build();

        this.logFormFields(form);

        return (Form<T>)form;
    }

    private void logFormFields(Form form) {
        if(LOG.isDebugEnabled()) {
            final Function<FormMember, String> mapper = (ff) -> {
                final Object value = ff.getValue();
                final List<SelectOption> choices = ff.getChoices();
                return ff.getName() + '=' + 
                        (choices==null||choices.isEmpty() ? value : 
                        (String.valueOf(value) + ", " + choices.size() + " choice(s)"));
            };
            LOG.debug("Form fields:{}", 
                    form.getMembers().stream()
                            .map(mapper)
                            .collect(Collectors.joining(", ")));
        }
    }
}
