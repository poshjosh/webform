package com.looseboxes.webform;

/**
 * @author hp
 */
public interface HttpSessionAttributes {
    
    String MODELOBJECT = "modelobject";
    String FORM = "form";
    
    String UPLOADED_FILES_PENDING = "webform_uploadedFilesPending";
    
    static String formReqParams(String formId) {
        return "params." + formId;
    }
}
