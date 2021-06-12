package com.looseboxes.webform.form;

import com.bc.webform.form.member.FormMemberBean;
import com.bc.webform.form.member.builder.FormMemberBuilderForJpaEntity;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class FormMemberBuilderImpl extends FormMemberBuilderForJpaEntity{

    private static final Logger LOG = LoggerFactory.getLogger(FormMemberBuilderImpl.class);
    
    private final PropertySearch propertySearch;
    
    public FormMemberBuilderImpl(PropertySearch propertySearch) {
        this.propertySearch = Objects.requireNonNull(propertySearch);
    }

    @Override
    public FormMemberBean<Field, Object> buildFormField() {
        
        final FormMemberBean<Field, Object> formMember = super.buildFormField();
        
        final Field field = this.getDataSource();
        
        this.propertySearch.find(WebformProperties.LABEL, field)
                .ifPresent((label) -> formMember.setLabel(label));

        this.propertySearch.find(WebformProperties.ADVICE, field)
                .ifPresent((advice) -> formMember.setAdvice(advice));
        
        final List<String> readOnlyFieldNames = this.propertySearch.findAll(
                WebformProperties.FIELD_READONLY_VALUES, field);
        
        if(LOG.isTraceEnabled()) {
            LOG.trace("Read only value(s) for: {}#{} = {}", 
                    field.getDeclaringClass().getSimpleName(), field.getName(), readOnlyFieldNames);
        }
        
        final boolean readOnlyValue = this.propertySearch.containsIgnoreCase(readOnlyFieldNames, field);
        
        LOG.trace("Is read only value: {}, field: {}#{}", readOnlyValue, 
                field.getDeclaringClass().getSimpleName(), field.getName());
        
        if(readOnlyValue) {
            formMember.setReadOnlyValue(Boolean.TRUE);
        }

        return formMember;
    }
}
