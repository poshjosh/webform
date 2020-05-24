package com.looseboxes.webform;

import static com.looseboxes.webform.CrudActionName.create;
import static com.looseboxes.webform.CrudActionName.read;
import static com.looseboxes.webform.CrudActionName.update;
import static com.looseboxes.webform.CrudActionName.delete;

/**
 * @author hp
 */
public interface FormEndpoints {
    
    /**
     * @return The endpoint to call when an error occurs
     */
    String getError();
    
    /**
     * @return The endpoint to call when a success occurs
     */
    String getSuccess();
    
    /**
     * @return The endpoint for displaying form inputs for
     */
    String getForm();
    
    /**
     * @return The endpoint for displaying form entries for user confirmation
     */
    String getFormConfirmation();
    
    /**
     * @return The endpoint for display form information initiated by the read
     * {@link com.looseboxes.webform.CrudActionName CRUDActionName}
     */
    String getFormData();
    
    /**
     * @param crudAction One of {@link com.looseboxes.webform.CrudActionName CRUDActionName}
     * @return The endpoint for the {@link com.looseboxes.webform.CrudActionName CRUDActionName}
     * @see com.looseboxes.webform.CrudActionName
     */
    default String forCrudAction(CrudActionName crudAction) {
        final String endpoint;
        switch(crudAction) {
            case create:
                endpoint = this.getForm(); break;
            case read:
                endpoint = this.getFormData(); break;
            case update:
                endpoint = this.getForm(); break;
            case delete:
                endpoint = this.getFormConfirmation(); break;
            default:
                throw Errors.unexpected(crudAction, (Object[])CrudActionName.values());
        }
        return endpoint;
    }
}
