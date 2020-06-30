package com.looseboxes.webform.form;

import com.looseboxes.webform.web.FormConfig;
import com.bc.webform.Form;
import com.bc.webform.FormMember;
import com.bc.webform.functions.FormInputContext;
import com.looseboxes.webform.exceptions.FormMemberNotFoundException;
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
    public FormConfig update(
            FormConfig formConfig, String memberName, Object memberValue) 
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
        
        final FormConfig formConfigUpdate = formConfig.writableCopy().form(formUpdate);
        
        LOG.debug("Updated: {}", formConfigUpdate);
        
        return formConfigUpdate;
    }
}
