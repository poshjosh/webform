package com.looseboxes.webform.form;

import com.bc.webform.Form;
import com.bc.webform.FormMember;
import com.bc.webform.functions.FormInputContext;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.Wrapper;
import com.looseboxes.webform.exceptions.FormMemberNotFoundException;
import com.looseboxes.webform.services.FormAttributeService;
import com.looseboxes.webform.store.StoreDelegate;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hp
 */
@Component
public class UpdateParentFormWithNewlyCreatedModel 
        implements Wrapper<StoreDelegate, UpdateParentFormWithNewlyCreatedModel>{

    private static final Logger LOG = LoggerFactory.getLogger(UpdateParentFormWithNewlyCreatedModel.class);
    
    private final FormAttributeService formAttributeService;
    private final FormInputContext<Object, Field, Object> formInputContext;

    @Autowired
    public UpdateParentFormWithNewlyCreatedModel(
            FormAttributeService formAttributeService,
            FormInputContext<Object, Field, Object> formInputContext) {
        this.formAttributeService = Objects.requireNonNull(formAttributeService);
        this.formInputContext = Objects.requireNonNull(formInputContext);
    }
    
    @Override
    public UpdateParentFormWithNewlyCreatedModel wrap(StoreDelegate delegate) {
        return new UpdateParentFormWithNewlyCreatedModel(
                this.formAttributeService.wrap(delegate),
                this.formInputContext);
    }

    @Override
    public StoreDelegate unwrap() {
        return this.formAttributeService.unwrap();
    }
    
    public Optional<FormConfig> updateParent(FormConfig formConfig) throws FormMemberNotFoundException{
        
        LOG.trace("#updateParent({})", formConfig);
        
        if(CRUDAction.create != formConfig.getCrudAction()) {
            LOG.debug("Only '" + CRUDAction.create + 
                    "' supported but found: " + formConfig.getCrudAction());
            return Optional.empty();
        }

        final Form<Object> form = Objects.requireNonNull(formConfig.getForm());
        
        final Form<Object> parent = form.getParent();
        if(parent == null) {
            LOG.debug("No parent to update for form: {}", form);
            return Optional.empty();
        }
        
        final FormMember parentMember = getParentMemberCorrespondingToForm(parent, form);
        
        final FormConfig formConfigUpdate = this.updateFormMember(
                parent, parentMember.getName(), formConfig.getModelobject());
        
        return Optional.of(formConfigUpdate);
    }
    
    /**
     * Return the member of the parent form corresponding to the form argument.
     * 
     * Given relationship: <code>User.address</code>, if parent is <code>User</code>
     * and form is <code>Address</code>, then this method returns the 
     * <code>address</code> member of the <code>User</code> form.
     * 
     * @param parent
     * @param form
     * @return 
     */
    public FormMember getParentMemberCorrespondingToForm(Form<Object> parent, Form<Object> form) {
        
        final String memberName = form.getName();
        
        FormMember formMember = parent.getMemberOptional(memberName).orElse(null);
        
        if(formMember == null) {
            final Class childType = form.getDataSource().getClass();
            final Predicate<FormMember> test = (member) -> 
                    childType.equals(((Field)member.getDataSource()).getType());
            formMember = parent.getMembers().stream()
                    .filter(test).findFirst().orElseThrow(
                    () -> formMemberNotFoundException(parent, "of type " + childType));
        }

        LOG.debug("Found {}#{} for form {}", 
                parent.getName(), formMember.getName(), form.getName());

        if(formMember == null) {
            throw formMemberNotFoundException(parent, memberName);
        }

        return formMember;
    }

    public FormConfig updateFormMember(
            Form<Object> form, String memberName, Object memberValue) 
            throws FormMemberNotFoundException{
        
        final String formid = form.getId();
        
        final FormConfig formConfig = this.formAttributeService
                .getSessionAttributeOrException(formid);
        
        final Object modelobject = formConfig.getModelobject();
        
        Objects.requireNonNull(modelobject);
        
        final FormMember formMember = form.getMemberOptional(memberName)
                .orElseThrow(() -> this.formMemberNotFoundException(form, memberName));

        final Field field = Objects.requireNonNull((Field)formMember.getDataSource());
        this.formInputContext.setValue(modelobject, field, memberValue);
        LOG.debug("Updated {}#{} to {}", form.getName(), memberName, memberValue);
        
        final FormMember formMemberUpdate = formMember.writableCopy().value(memberValue);
        
        final Form<Object> formUpdate = form.writableCopy()
                .dataSource(modelobject)
                .replaceMember(formMemberUpdate);
        
        final FormConfig formConfigUpdate = formConfig.writableCopy().form(formUpdate);
        
        this.formAttributeService.setSessionAttribute(formConfigUpdate);
        
        LOG.debug("Updated: {}", formConfigUpdate);
        
        return formConfigUpdate;
    }

    private FormMemberNotFoundException formMemberNotFoundException(Object form, String formMemberName) {
        final String msg = "FormMember: " + formMemberName + ", not found for form: " + form;
        return new FormMemberNotFoundException(msg);
    }
}
