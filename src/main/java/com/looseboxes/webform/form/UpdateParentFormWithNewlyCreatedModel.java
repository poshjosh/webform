package com.looseboxes.webform.form;

import com.bc.webform.form.Form;
import com.bc.webform.form.member.FormMember;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.exceptions.FormMemberNotFoundException;
import com.looseboxes.webform.services.FormAttributeService;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.web.FormRequest;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class UpdateParentFormWithNewlyCreatedModel {
    
    private static final Logger LOG = LoggerFactory.getLogger(UpdateParentFormWithNewlyCreatedModel.class);
    
    private final FormMemberUpdater formMemberUpdater;

    public UpdateParentFormWithNewlyCreatedModel(FormMemberUpdater formMemberUpdater) {
        this.formMemberUpdater = Objects.requireNonNull(formMemberUpdater);
    }
    
    public boolean updateParent(FormRequest formRequest) {
        
        FormConfigDTO formConfig = formRequest.getFormConfig();
    
        boolean updated = false;
        
        if(formConfig.getForm().getParent() != null &&
                formConfig.getModelobject() != null) {

            final Form<Object> form = Objects.requireNonNull(formConfig.getForm());

            final Form<Object> parentForm = form.getParent();

            if(parentForm == null) {
                
                LOG.debug("No parent to update for form: {}", form);
                
            }else{
            
                final String parentFormId = parentForm.getId();
                
                final FormAttributeService formAttributeService = formRequest.getAttributeService();

                FormConfigDTO parentFormConfig = formAttributeService
                        .getSessionAttributeOrException(parentFormId);

                if(parentFormConfig != null) {

                    try{

                        parentFormConfig = this.updateParent(formConfig, parentFormConfig);

                        formAttributeService.setSessionAttribute(parentFormConfig);
                        
                        updated = true;

                    }catch(RuntimeException e) {
                        LOG.warn("Failed to update parent with this form's value", e);
                    }
                }
            }
        }
        
        return updated;
    }
    
    
    public FormConfigDTO updateParent(FormConfigDTO formConfig, FormConfigDTO parentFormConfig) 
            throws FormMemberNotFoundException{
        
        LOG.trace("#updateParent({})", formConfig);
        
        if(CRUDAction.create != formConfig.getCrudAction()) {
            LOG.debug("Only '" + CRUDAction.create + 
                    "' supported but found: " + formConfig.getCrudAction());
            return parentFormConfig;
        }

        final Form<Object> form = Objects.requireNonNull(formConfig.getForm());
        
        final Form<Object> parent = parentFormConfig.getForm();
        if(parent == null) {
            LOG.debug("No parent to update for form: {}", form);
            return parentFormConfig;
        }
        
        final FormMember parentMember = getParentMemberCorrespondingToForm(parent, form);
        
        final FormConfigDTO parentFormConfigUpdate = this.formMemberUpdater.update(
                parentFormConfig, parentMember.getName(), formConfig.getModelobject());
        
        return parentFormConfigUpdate;
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
                    () -> FormMemberNotFoundException.from(parent, "of type " + childType));
        }

        LOG.debug("Found {}#{} for form {}", 
                parent.getName(), formMember.getName(), form.getName());

        if(formMember == null) {
            throw FormMemberNotFoundException.from(parent, memberName);
        }

        return formMember;
    }
}
