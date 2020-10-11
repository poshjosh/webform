package com.looseboxes.webform.form.util;

import com.looseboxes.webform.web.FormRequest;
import java.util.List;

/**
 * @author hp
 */
public interface ModelObjectImagePathsProvider{
    
    List<String> getImagePathsOfRootAndNestedEntities(FormRequest<Object> formRequest);
    
    List<String> getImagePathsOfRootEntityOnly(FormRequest<Object> formRequest);
}
