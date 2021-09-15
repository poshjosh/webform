package com.looseboxes.webform.form;

import com.bc.webform.form.Form;

/**
 * @author hp
 */
public interface FormFactory {
    <T> Form<T> newForm(Form<T> parentForm, String id, String name, T domainObject);
}
