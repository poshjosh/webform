package com.looseboxes.webform.web;

import com.looseboxes.webform.services.AttributeService;
import com.looseboxes.webform.store.AttributeStore;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hp
 */
public interface WebRequest<T> {
    
    String getSessionId();
    
    String getParameter(String name);
    
    String [] getParameterValues(String name);
    
    default AttributeStore<ModelMap> modelAttributes() {
        return this.getAttributeService().modelAttributes();
    }
    
    default AttributeStore<HttpServletRequest> requestAttributes() {
        return this.getAttributeService().requestAttributes();
    }

    default AttributeStore<HttpSession> sessionAttributes() {
        return this.getAttributeService().sessionAttributes();
    }

    AttributeService getAttributeService();
    
    boolean hasFiles();
    
    Map<String, MultipartFile> getFiles();
    
    Locale getLocale();
    
    Map<String, List<MultipartFile>> getMultiValueFiles();
}
