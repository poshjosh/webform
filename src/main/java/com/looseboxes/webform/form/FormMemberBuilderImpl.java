package com.looseboxes.webform.form;

import com.bc.webform.form.member.FormMember;
import com.bc.webform.form.member.FormMemberBean;
import com.bc.webform.form.member.builder.FormMemberBuilderForJpaEntity;
import com.bc.webform.form.member.FormInputContext;
import com.bc.webform.form.member.MultiChoiceContext;
import com.bc.webform.form.member.ReferencedFormContext;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author hp
 */
public class FormMemberBuilderImpl extends FormMemberBuilderForJpaEntity{

    private final PropertySearch propertySearch;
    
    public FormMemberBuilderImpl(
            PropertySearch propertySearch, 
            FormInputContext<Object, Field, Object> formInputContext,
            MultiChoiceContext<Object, Field> multiChoiceContext,
            ReferencedFormContext referencedFormContext) {
        
        super(formInputContext, multiChoiceContext);
        
        this.propertySearch = Objects.requireNonNull(propertySearch);
        
        this.referencedFormContext(referencedFormContext);
    }
    
    @Override
    public FormMember<Field, Object> build() {
        
        final FormMember<Field, Object> forUpdate = super.build();
        
        final FormMemberBean<Field, Object> update = forUpdate.writableCopy();

        final Field field = this.getDataSource();
        
        this.propertySearch.find(WebformProperties.LABEL, field)
                .ifPresent((label) -> update.setLabel(label));

        this.propertySearch.find(WebformProperties.ADVICE, field)
                .ifPresent((advice) -> update.setAdvice(advice));
                
        return update;
    }
}
