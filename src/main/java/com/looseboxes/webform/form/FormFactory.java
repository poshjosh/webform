package com.looseboxes.webform.form;

import com.bc.webform.Form;

/**
 * @author hp
 */
public interface FormFactory {
    
    Form newForm(Form parent, String id, String name);
    
    Form newForm(Form parent, String id, String name, Object object);
}
