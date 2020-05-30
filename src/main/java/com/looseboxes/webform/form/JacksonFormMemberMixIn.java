package com.looseboxes.webform.form;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author hp
 */
@JsonFilter(JacksonFormMemberFilter.FILTER_ID)
public class JacksonFormMemberMixIn {
    
}
