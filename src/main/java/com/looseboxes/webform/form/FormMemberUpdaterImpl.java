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
import java.util.function.UnaryOperator;
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
            FormConfigDTO formConfig, String memberName, 
            UnaryOperator<FormMemberBean> updater) throws FormMemberNotFoundException{
        
        final FormMember formMember = this.getFormMember(formConfig, memberName);
        
        final FormMember formMemberUpdate = updater.apply(formMember.writableCopy());
        
        this.replaceFormMember(formConfig, formMemberUpdate);
        
        LOG.debug("After updating value in form member and data source\n{}", formConfig);
        
        return formConfig;
    }
    
    @Override
    public FormConfigDTO setValue(
            FormConfigDTO formConfig, String memberName, Object memberValue) 
            throws FormMemberNotFoundException{
        
        final Object modelobject = this.checkFormConfig(formConfig);
        
        final FormMember formMember = this.getFormMember(formConfig, memberName);

        final Field field = Objects.requireNonNull((Field)formMember.getDataSource());
        
        this.formInputContext.setValue(modelobject, field, memberValue);
        
        LOG.debug("Updated {}#{} to {}", formConfig.getForm().getName(), memberName, memberValue);
        
        final FormMember formMemberUpdate = formMember.writableCopy().value(memberValue);
        
        this.replaceFormMember(formConfig, formMemberUpdate);
        
        LOG.debug("After updating value in form member and data source\n{}", formConfig);
        
        return formConfig;
    }

    @Override
    public FormConfigDTO setChoices(
            FormConfigDTO formConfig, String memberName, List<SelectOption> choices) 
            throws FormMemberNotFoundException{

        this.checkFormConfig(formConfig);
        
        final FormMember formMember = this.getFormMember(formConfig, memberName);

        final FormMember formMemberUpdate = formMember.writableCopy()
                .choices(choices).multiChoice(true);
        
        LOG.debug("Updated {}#{} choices to {}", formConfig.getForm().getName(), memberName, choices);

        this.replaceFormMember(formConfig, formMemberUpdate);
        
        LOG.debug("After updating form member choices\n{}", formConfig);
        
        return formConfig;
    }
    
    private Object checkFormConfig(FormConfigDTO formConfig) {
        Objects.requireNonNull(formConfig.getForm());
        return Objects.requireNonNull(formConfig.getModelobject());
    }    
    
    private FormMember getFormMember(FormConfigDTO formConfig, String memberName) 
            throws FormMemberNotFoundException{
        
        final Form<Object> form = formConfig.getForm();
        
        final FormMember formMember = form.getMemberOptional(memberName)
                .orElseThrow(() -> FormMemberNotFoundException.from(form, memberName));
        
        return formMember;
    }    

    private FormConfigDTO replaceFormMember(
            FormConfigDTO formConfig, FormMember formMember){
        
        final Object modelobject = this.checkFormConfig(formConfig);
        
        final Form<Object> formUpdate = formConfig.getForm().writableCopy()
                .dataSource(modelobject)
                .replaceMember(formMember);
        
        formConfig.setForm(formUpdate);
        
        LOG.debug("After updating form member choices\n{}", formConfig);
        
        return formConfig;
    }
}
