package com.looseboxes.webform.form;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * @author hp
 */
public interface FormFieldTest extends Predicate<Field>{

    @Override
    boolean test(Field field);
}
