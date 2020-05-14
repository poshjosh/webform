package com.looseboxes.webform;

/**
 * @author hp
 */
public interface SessionAttributes {
    String MODELOBJECT = ModelAttributes.MODELOBJECT;
    String FORM_CONFIG_PREFIX = "form";
    String UPLOADED_FILES_PENDING = "webform_uploadedFilesPending";
    static String forFormId(String s) {
        return FORM_CONFIG_PREFIX + "_" + s;
    }
}
