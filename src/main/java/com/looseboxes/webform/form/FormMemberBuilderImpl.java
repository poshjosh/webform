package com.looseboxes.webform.form;

import com.bc.webform.form.member.FormMember;
import com.bc.webform.form.member.FormMemberBean;
import com.bc.webform.form.member.FormMemberBuilderForJpaEntity;
import com.bc.webform.form.member.FormInputContext;
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
    private final FormInputContext<Object, Field, Object> formInputContext;
    private final ReferencedFormContext referencedFormContext;
    
    public FormMemberBuilderImpl(
            PropertySearch propertySearch, 
            FormInputContext<Object, Field, Object> formInputContext,
            ReferencedFormContext referencedFormContext) {
        this.propertySearch = Objects.requireNonNull(propertySearch);
        this.formInputContext = Objects.requireNonNull(formInputContext);
        this.referencedFormContext = Objects.requireNonNull(referencedFormContext);
    }
    
    @Override
    public FormMember<Field, Object> build() {
        
        if(this.getReferencedFormContext() == null) {
            this.referencedFormContext(this.referencedFormContext);
        }
        
        if(this.getFormInputContext() == null) {
            this.formInputContext(formInputContext);
        }
        
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
