package com.looseboxes.webform.web;

import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.services.AttributeService;
import com.looseboxes.webform.store.AttributeStore;
import java.util.List;
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
    
    // @TODO replace all code using this deprecated method 
    /**
     * @return The ModelMap for the web request
     * @deprecated Do not access the ModelMap directly, rather call method 
     * {@link #modelAttributes()} and use the returned 
     * {@link com.looseboxes.webform.store.AttributeStore AttributeStore}
     */
    @Deprecated
    ModelMap getModelMap();
    
    default T getModelObject() {
        return (T)this.getAttributeService()
                .sessionAttributes()
                .getOrDefault(HttpSessionAttributes.MODELOBJECT, null);
    }
    
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
    
    Map<String, List<MultipartFile>> getMultiValueFiles();
}
