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
    
    private static final class SetValue implements UnaryOperator<FormMemberBean>{

        private final FormInputContext<Object, Field, Object> formInputContext;
        
        private final Object value;

        public SetValue(FormInputContext<Object, Field, Object> formInputContext, Object value) {
            this.formInputContext = Objects.requireNonNull(formInputContext);
            this.value = value;
        }
        
        @Override
        public FormMemberBean apply(FormMemberBean formMember) {
            
            final Field field = Objects.requireNonNull((Field)formMember.getDataSource());

            final Object modelobject = Objects.requireNonNull(formMember.getForm().getDataSource());
            
            this.formInputContext.setValue(modelobject, field, value);

            return formMember.value(value);
        }
    }

    private static final class SetChoices implements UnaryOperator<FormMemberBean>{
        
        private final List<SelectOption> choices;

        public SetChoices(List<SelectOption> choices) {
            this.choices = choices;
        }
        
        @Override
        public FormMemberBean apply(FormMemberBean formMember) {
            return formMember.choices(choices);
        }
    }
    
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
        
        final UnaryOperator<FormMemberBean> updater = 
                new SetValue(this.formInputContext, memberValue);
        
        return this.update(formConfig, memberName, updater);
    }
    
    @Override
    public FormConfigDTO setChoices(
            FormConfigDTO formConfig, String memberName, List<SelectOption> choices) 
            throws FormMemberNotFoundException{

        final UnaryOperator<FormMemberBean> updater = new SetChoices(choices);
        
        return this.update(formConfig, memberName, updater);
    }

    @Override
    public FormConfigDTO update(
            FormConfigDTO formConfig, String memberName, 
            UnaryOperator<FormMemberBean> updater) throws FormMemberNotFoundException{
        
        final FormMember formMember = this.getFormMember(formConfig, memberName);
        
        final FormMember formMemberUpdate = updater.apply(formMember.writableCopy());
        
        this.replaceFormMember(formConfig, formMemberUpdate);
        
        LOG.debug("Before: {}", formConfig.getForm());
        
        final Form form = formFactory.newForm(
                formConfig.getForm().getParent(), 
                formConfig.getForm().getId(), 
                formConfig.getModelname(), 
                formConfig.getModelobject());

        formConfig.setForm(form);
        
        LOG.debug(" After: {}", formConfig.getForm());
        
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
