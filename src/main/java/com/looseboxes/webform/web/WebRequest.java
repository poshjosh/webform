package com.looseboxes.webform.web;

import com.looseboxes.webform.services.AttributeService;
import java.util.List;
import java.util.Map;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hp
 */
public interface WebRequest<T> {
    
    String getSessionId();
    
    ModelMap getModelMap();
    
    T getModelObject();
    
    String getParameter(String name);
    
    String [] getParameterValues(String name);
    
    AttributeService getAttributeService();
    
    boolean hasFiles();
    
    Map<String, MultipartFile> getFiles();
    
    Map<String, List<MultipartFile>> getMultiValueFiles();
}
