package com.looseboxes.webform.form.util;

import com.looseboxes.webform.web.FormRequest;
import java.util.List;

/**
 * @author hp
 */
public interface ModelObjectImagePathsProvider{
    
    List<String> getImagePaths(FormRequest<Object> formRequest);
}
