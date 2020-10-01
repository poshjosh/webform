package com.looseboxes.webform.form;

import com.bc.webform.choices.SelectOption;
import com.bc.webform.form.FormBean;
import com.bc.webform.form.member.FormInputContext;
import com.bc.webform.form.member.FormMemberBean;
import com.looseboxes.webform.exceptions.FormMemberNotFoundException;
import com.looseboxes.webform.web.FormConfigDTO;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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
    public FormConfigDTO setValue(
            FormConfigDTO formConfig, String memberName, Object memberValue) 
            throws FormMemberNotFoundException{
        
        final Function<FormMemberBean, FormBean> updater = (formMember) -> 
                setValue(formMember, memberValue).getForm();

        return this.update(formConfig, memberName, updater);
    }
    
    private FormMemberBean setValue(FormMemberBean formMember, Object memberValue) {

        FormBean form = formMember.getForm();
        
        final Object modelobject = Objects.requireNonNull(form.getDataSource());

        final Field field = Objects.requireNonNull((Field)formMember.getDataSource());

        this.formInputContext.setValue(modelobject, field, memberValue);
        
        LOG.debug("Set {}#{} to {}", form.getName(), field.getName(), memberValue);

        return formMember;
    }

    @Override
    public FormConfigDTO setChoices(
            FormConfigDTO formConfig, String memberName, List<SelectOption> choices) 
            throws FormMemberNotFoundException{

        final Function<FormMemberBean, FormBean> updater = (formMember) -> 
                formMember.choices(choices).multiChoice(true).getForm();
        
        
        return this.update(formConfig, memberName, updater);
    }

    @Override
    public FormConfigDTO update(
            FormConfigDTO formConfig, String memberName, 
            Function<FormMemberBean, FormBean> updater) throws FormMemberNotFoundException{
        
        LOG.trace("Before updating {}.{}. {}", 
                formConfig.getModelname(), memberName, formConfig.getForm());
        
        final FormMemberBean formMember = this.getFormMember(formConfig, memberName);
        
        
        final FormBean formUpdate = updater.apply(formMember);
        
        formConfig.setForm(formUpdate);
        
        LOG.debug(" After updating {}.{}. {}", 
                formConfig.getModelname(), memberName, formConfig.getForm());
        
        return formConfig;
    }
    
    private FormMemberBean getFormMember(FormConfigDTO formConfig, String memberName) 
            throws FormMemberNotFoundException{
        
        final FormBean<Object> form = formConfig.getForm();

        final FormMemberBean formMember = form.getMemberOptional(memberName)
                .orElseThrow(() -> FormMemberNotFoundException.from(form, memberName));
        
        return formMember;
    }    
}
