package com.looseboxes.webform.form;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.form.Form;
import com.bc.webform.form.member.ReferencedFormContext;
import com.bc.webform.TypeTests;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import com.looseboxes.webform.CRUDAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class ReferencedFormContextImpl implements ReferencedFormContext<Object, Field>{
    
    private static final Logger LOG = LoggerFactory.getLogger(ReferencedFormContextImpl.class);
    
    private final TypeTests typeTests;
    private final TypeFromNameResolver typeFromNameResolver;

    public ReferencedFormContextImpl(
            TypeTests typeTests, 
            TypeFromNameResolver typeFromNameResolver) {
        this.typeTests = Objects.requireNonNull(typeTests);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
    }

    @Override
    public boolean isReferencedType(Form<Object> form, Field field) {
        final Class fieldType = field.getType();
        boolean result = ! typeTests.isEnumType(fieldType) && typeTests.isDomainType(fieldType);
        LOG.trace("Is referenced type: {}, {}.{}", 
                result, (form==null?null:form.getName()), field.getName());
        return result;
    }

    @Override
    public Optional<String> getReferencedFormHref(Form<Object> form, Field field) {
        final String name = this.typeFromNameResolver.getName(field.getType());
        final StringBuilder b = new StringBuilder()
                .append('/').append(CRUDAction.create)
                .append('/').append(name);
        return Optional.of(b.toString());
    }
}
