package com.looseboxes.webform.form;

import com.bc.webform.Form;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author hp
 */
public interface FormFieldChoices {

    Map getChoices(Form form, Object object, Field field);

    boolean hasValues(Form form, Object object, Field field);
}
