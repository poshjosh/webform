package com.looseboxes.webform.form;

import com.bc.webform.Form;

/**
 * @author hp
 */
public interface FormFactory {
    
    Form newForm(String name);
    
    Form newForm(String name, Object object);
}
