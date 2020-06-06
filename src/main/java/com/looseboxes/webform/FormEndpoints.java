package com.looseboxes.webform;

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
     * {@link com.looseboxes.webform.CrudEvent CRUDActionName}
     */
    String getFormData();
    
    /**
     * @param crudAction One of {@link com.looseboxes.webform.CrudEvent CRUDActionName}
     * @return The endpoint for the {@link com.looseboxes.webform.CrudEvent CRUDActionName}
     * @see com.looseboxes.webform.CrudEvent
     */
    default String forCrudAction(CrudEvent crudAction) {
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
                throw Errors.unexpected(crudAction, (Object[])CrudEvent.values());
        }
        return endpoint;
    }
}
