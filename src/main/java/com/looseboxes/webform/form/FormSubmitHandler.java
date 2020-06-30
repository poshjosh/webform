package com.looseboxes.webform.form;

import com.looseboxes.webform.web.FormConfig;

/**
 * This interface's {@link #process(com.looseboxes.webform.form.FormConfig)} 
 * method will be called after validation as part of the submit process. 
 *
 * Make sure you provide an implementation for this interface.
 * Use it to update the database etc.
 * 
 * @author hp
 */
@FunctionalInterface
public interface FormSubmitHandler {

    void process(FormConfig formConfig);
}
