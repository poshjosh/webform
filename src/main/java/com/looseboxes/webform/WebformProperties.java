package com.looseboxes.webform;

import java.util.Objects;

/**
 * @author hp
 */
public interface WebformProperties {

    String PROPERTY_PREFIX = "webform";
    
    String FIELDS_TO_IGNORE = "webform.field.ignores";
    
    String FIELD_DEFAULT_VALUE = "field.default.value";

    String DEFAULT_FIELDS = "field.defaults";
    
    String FIELD_TYPE = "field.type";
    
    String MAX_ITEMS_IN_MULTICHOICE = "maxItemsInMultichoice";

    String FORMATS_DATETIME = "formats.datetime";
    String FORMATS_DATE = "formats.date";
    String FORMATS_TIME = "formats.time";

    String LABEL = "label";
    String ADVICE = "advice";
    
    static String withPrefix(String name) {
        Objects.requireNonNull(name);
        return PROPERTY_PREFIX + '.' + name;
    }
}
