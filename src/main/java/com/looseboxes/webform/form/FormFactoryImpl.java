package com.looseboxes.webform.form;

import com.bc.webform.form.Form;
import com.bc.webform.form.FormBean;
import java.util.Objects;
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

        if(LOG.isTraceEnabled()) {
            LOG.trace("{}", ((FormBean)form).print());
        }

        return (Form<T>)form;
    }
}
