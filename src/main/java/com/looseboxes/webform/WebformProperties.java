package com.looseboxes.webform;

/**
 * @author hp
 */
public interface WebformProperties {

    String _PREFIX = "webform.";
    
    String ENUM_TYPE = _PREFIX + "enumType";
    
    String FIELDS_TO_IGNORE = _PREFIX + "field.ignores";
    
    String FIELD_DEFAULT_VALUE = _PREFIX + "field.value.default";

    String DEFAULT_FIELDS = _PREFIX + "field.name.selection.defaults";
    
    String FIELD_TYPE = _PREFIX + "field.type";
    
    String MAX_ITEMS_IN_MULTICHOICE = _PREFIX + "maxItemsInMultichoice";

    String FORMATS_DATETIME = _PREFIX + "formats.datetime";
    String FORMATS_DATE = _PREFIX + "formats.date";
    String FORMATS_TIME = _PREFIX + "formats.time";

    String LABEL = _PREFIX + "field.label";
    
    String ADVICE = _PREFIX + "field.advice";
    
    String ORDER = _PREFIX + "field.order";
}
