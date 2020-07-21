package com.looseboxes.webform.form;

import com.bc.webform.form.Form;
import com.bc.webform.form.member.FormMember;
import com.bc.webform.form.member.FormInputContext;
import com.looseboxes.webform.exceptions.FormMemberNotFoundException;
import com.looseboxes.webform.web.FormConfigDTO;
import java.lang.reflect.Field;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class FormMemberUpdaterImpl implements FormMemberUpdater {
    
    private static final Logger LOG = LoggerFactory.getLogger(FormMemberUpdaterImpl.class);
    
    private final FormInputContext<Object, Field, Object> formInputContext;

    public FormMemberUpdaterImpl(
            FormInputContext<Object, Field, Object> formInputContext) {
        this.formInputContext = Objects.requireNonNull(formInputContext);
    }
    
    @Override
    public FormConfigDTO update(
            FormConfigDTO formConfig, String memberName, Object memberValue) 
            throws FormMemberNotFoundException{
        
        final Object modelobject = formConfig.getModelobject();
        
        Objects.requireNonNull(modelobject);
        
        final Form<Object> form = formConfig.getForm();
        
        final FormMember formMember = form.getMemberOptional(memberName)
                .orElseThrow(() -> FormMemberNotFoundException.from(form, memberName));

        final Field field = Objects.requireNonNull((Field)formMember.getDataSource());
        
        this.formInputContext.setValue(modelobject, field, memberValue);
        
        LOG.debug("Updated {}#{} to {}", form.getName(), memberName, memberValue);
        
        final FormMember formMemberUpdate = formMember.writableCopy().value(memberValue);
        
        final Form<Object> formUpdate = form.writableCopy()
                .dataSource(modelobject)
                .replaceMember(formMemberUpdate);
        
        formConfig.setForm(formUpdate);
        
        LOG.debug("After updating form data source\n{}", formConfig);
        
        return formConfig;
    }
}
