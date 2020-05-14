package com.looseboxes.webform.form;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.Form;
import com.bc.webform.FormBuilder;
import com.bc.webform.FormField;
import com.bc.webform.functions.FormFieldsCreator;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class FormFactoryImpl implements FormFactory{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormFactoryImpl.class);
    
    private final FormBuilder formBuilder;
    
    private final TypeFromNameResolver typeFromNameResolver;
    
    private final FormFieldsCreator formFieldsCreator;
    
    private final Comparator<FormField> formFieldComparator;
    
    public FormFactoryImpl(
            FormBuilder formBuilder,
            TypeFromNameResolver typeFromNameResolver,
            FormFieldsCreator formFieldsCreator,
            Comparator<FormField> formFieldComparator) {
        this.formBuilder = Objects.requireNonNull(formBuilder);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
        this.formFieldsCreator = Objects.requireNonNull(formFieldsCreator);
        this.formFieldComparator = Objects.requireNonNull(formFieldComparator);
    }
    
    @Override
    public Form newForm(String name) {
        return this.newForm(name, typeFromNameResolver.newInstance(name));
    }
    
    @Override
    public Form newForm(String name, Object domainObject) {
    
        try{
            
            final com.bc.webform.Form form = this.formBuilder
                    .withDefault(name)
                    .fieldsCreator(this.formFieldsCreator)
                    .fieldsComparator(this.formFieldComparator)
                    .fieldDataSource(domainObject).build();
            
            LOG.debug("Form fields:\n{}", 
                    form.getFormFields().stream()
                            .map((formField) -> formField.getName() + '=' + formField.getValue())
                            .collect(Collectors.joining("\n")));
            
            return form;
            
        }catch(RuntimeException e) {
            
            throw e;
        }
    }
}
