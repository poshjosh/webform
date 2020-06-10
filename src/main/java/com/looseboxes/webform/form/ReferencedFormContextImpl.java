package com.looseboxes.webform.form;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.Form;
import com.bc.webform.functions.ReferencedFormContext;
import com.bc.webform.functions.TypeTests;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import com.looseboxes.webform.CRUDAction;

/**
 * @author hp
 */
public class ReferencedFormContextImpl implements ReferencedFormContext<Object, Field>{
    
    private final TypeTests typeTests;
    private final TypeFromNameResolver typeFromNameResolver;

    public ReferencedFormContextImpl(
            TypeTests typeTests, 
            TypeFromNameResolver typeFromNameResolver) {
        this.typeTests = Objects.requireNonNull(typeTests);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
    }

    @Override
    public boolean isReferencedType(Object formDataSource, Field dataSourceField) {
        final Class fieldType = dataSourceField.getType();
        return ! typeTests.isEnumType(fieldType) && typeTests.isDomainType(fieldType);
    }

    @Override
    public Optional<Form> createReferencedForm(
            Form form, Object formDataSource, Field dataSourceField) {
//        FormBuilder<Object, Field, Object> b;
//        final Form<Object> refForm = b.applyDefaults(dataSourceField.getName())
//                .dataSource(dataSourceField.getDeclaringClass().newInstance())
//                .formMemberBuilder(formFieldBuilder)
//                .formMemberComparator(comparator)
//                .formMemberTest(test)
//                .id(id)
//                .parent(form)
//                .sourceFieldsProvider(sourceFieldsProvider)
//                .build();
        return Optional.empty();
    }

    @Override
    public Optional<String> getReferencedFormHref(
            Form form, Object formDataSource, Field field) {
        final String name = this.typeFromNameResolver.getName(field.getType());
        final StringBuilder b = new StringBuilder()
                .append('/').append(CRUDAction.create)
                .append('/').append(name);
        return Optional.of(b.toString());
    }
}
