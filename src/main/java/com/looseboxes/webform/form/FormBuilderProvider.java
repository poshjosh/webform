package com.looseboxes.webform.form;

import com.bc.webform.form.FormBuilder;
import java.lang.reflect.Field;

/**
 * @author hp
 */
@FunctionalInterface
public interface FormBuilderProvider{
    
    FormBuilder<Object, Field, Object> get();
}
