package com.looseboxes.webform.form.util;

import com.looseboxes.webform.Errors;
import java.util.Objects;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.domain.UpdateEntityAndNestedIfAny;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.web.FormRequest;

/**
 * @author hp
 */
public class FormSubmitHandlerImpl implements FormSubmitHandler{
    
    private final UpdateEntityAndNestedIfAny saveEntityAndChildrenIfAny;
    
    public FormSubmitHandlerImpl(UpdateEntityAndNestedIfAny saveEntityAndChildrenIfAny) {
        this.saveEntityAndChildrenIfAny = Objects.requireNonNull(saveEntityAndChildrenIfAny);
    }
    
    @Override
    public void process(FormRequest formRequest) {
        
        FormConfigDTO formConfig = formRequest.getFormConfig();
                    
        final CRUDAction crudAction = formConfig.getCrudAction();
        switch(crudAction) {
            case create:
                saveEntityAndChildrenIfAny.save(formRequest);
                break;
                
            case read:
                break;
                
            case update:
                saveEntityAndChildrenIfAny.save(formRequest);
                break;
                
            case delete:
                // We delete images in the root entity only
                // This is because We we delete a product and the product
                // has a nested user, then we need not delete the product's user
                //
                this.saveEntityAndChildrenIfAny.deleteRootOnly(formRequest);
                break;
                
            default:
                throw Errors.unexpectedElement(crudAction, CRUDAction.values());
        }   
    }
}
