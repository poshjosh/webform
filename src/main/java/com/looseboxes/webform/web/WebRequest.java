package com.looseboxes.webform.web;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hp
 */
public interface WebRequest<T> {
    
    String getSessionId();
    
    String getParameter(String name);
    
    String [] getParameterValues(String name);
    
    boolean hasFiles();
    
    Map<String, MultipartFile> getFiles();
    
    Locale getLocale();
    
    Map<String, List<MultipartFile>> getMultiValueFiles();
}
