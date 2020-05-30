package com.looseboxes.webform.form;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author hp
 */
@JsonFilter(JacksonDomainTypeFilter.FILTER_ID)
public class JacksonFormMixIn {
    
}
