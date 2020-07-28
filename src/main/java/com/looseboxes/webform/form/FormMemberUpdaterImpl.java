package com.looseboxes.webform.form;

import com.bc.webform.choices.SelectOption;
import com.bc.webform.form.Form;
import com.bc.webform.form.member.FormMember;
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
    
    private final FormFactory formFactory;

    public FormMemberUpdaterImpl(
            FormInputContext<Object, Field, Object> formInputContext,
            FormFactory formFactory) {
        this.formInputContext = Objects.requireNonNull(formInputContext);
        this.formFactory = Objects.requireNonNull(formFactory);
    }

    @Override
    public FormConfigDTO setValue(
            FormConfigDTO formConfig, String memberName, Object memberValue) 
            throws FormMemberNotFoundException{
        
        final Function<FormMemberBean, Form> updater = (formMember) -> 
                setValue(formConfig.getModelname(), formMember, memberValue);

        return this.update(formConfig, memberName, updater);
    }
    
    private Form setValue(String modelname, FormMemberBean formMember, Object memberValue) {

        final Object modelobject = Objects.requireNonNull(formMember.getForm().getDataSource());

        final Field field = Objects.requireNonNull((Field)formMember.getDataSource());

        this.formInputContext.setValue(modelobject, field, memberValue);
        
        final Form form = formMember.getForm();

        LOG.debug("Set {}.{} to {}", form.getName(), field.getName(), memberValue);

        final Form formUpdate = formFactory.newForm(
                form.getParent(), 
                form.getId(), 
                modelname, 
                form.getDataSource());

        return formUpdate;
    }

    @Override
    public FormConfigDTO setChoices(
            FormConfigDTO formConfig, String memberName, List<SelectOption> choices) 
            throws FormMemberNotFoundException{

        final Function<FormMemberBean, Form> updater = (formMember) -> 
                formMember.choices(choices).multiChoice(true).getForm();
        
        return this.update(formConfig, memberName, updater);
    }

    @Override
    public FormConfigDTO update(
            FormConfigDTO formConfig, String memberName, 
            Function<FormMemberBean, Form> updater) throws FormMemberNotFoundException{
        
        LOG.trace("Before updating {}.{}. {}", 
                formConfig.getModelname(), memberName, formConfig.getForm());
        
        final FormMember formMember = this.getFormMember(formConfig, memberName);
        
        final Form formUpdate = updater.apply(formMember.writableCopy());
        
//        formUpdate = formUpdate.writableCopy()
//                .dataSource(formConfig.getModelobject())
//                .replaceMember(formMemberUpdate);
        
        formConfig.setForm(formUpdate);
        
        LOG.debug(" After updating {}.{}. {}", 
                formConfig.getModelname(), memberName, formConfig.getForm());
        
        return formConfig;
    }
    
    private FormMember getFormMember(FormConfigDTO formConfig, String memberName) 
            throws FormMemberNotFoundException{
        
        final Form<Object> form = formConfig.getForm();

        final FormMember formMember = form.getMemberOptional(memberName)
                .orElseThrow(() -> FormMemberNotFoundException.from(form, memberName));
        
        return formMember;
    }    
}
