package com.looseboxes.webform.json;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author hp
 */
@JsonFilter(JacksonFormFilter.FILTER_ID)
public class JacksonFormMixIn {
    
}
