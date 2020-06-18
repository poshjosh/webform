package com.looseboxes.webform.json;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author hp
 */
@JsonFilter(JacksonFormMemberFilter.FILTER_ID)
public class JacksonFormMemberMixIn {
    
}
