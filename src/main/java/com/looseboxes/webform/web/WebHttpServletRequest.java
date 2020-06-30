package com.looseboxes.webform.web;

import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.services.AttributeService;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

/**
 * @author hp
 */
public class WebHttpServletRequest<T> implements WebRequest<T>{
    
    private final HttpServletRequest request;
    
    private final ModelMap modelMap;
    
    private final Map<String, MultipartFile> files;

    private final Map<String, List<MultipartFile>> multiValueFiles;

    private final AttributeService attributeService;
    
    public WebHttpServletRequest(
            HttpServletRequest request, ModelMap modelMap, AttributeService attributeService) {
        this.request = Objects.requireNonNull(request);
        this.modelMap = Objects.requireNonNull(modelMap);
        if(request instanceof MultipartRequest) {
            MultipartRequest multiPartRequest = (MultipartRequest)request;
            this.files = multiPartRequest.getFileMap();
            this.multiValueFiles = multiPartRequest.getMultiFileMap();
        }else{
            this.files = Collections.EMPTY_MAP;
            this.multiValueFiles = Collections.EMPTY_MAP;
        }
        this.attributeService = attributeService.wrap(new StoreDelegate(modelMap, request));
    }

    @Override
    public boolean hasFiles() {
        return ! this.isNullEmpty(files) || ! this.isNullEmpty(this.multiValueFiles);
    }
    
    private boolean isNullEmpty(Map m) {
        return m == null || m.isEmpty();
    }

    @Override
    public ModelMap getModelMap() {
        return modelMap;
    }

    @Override
    public String getParameter(String string) {
        return request.getParameter(string);
    }

    @Override
    public String[] getParameterValues(String string) {
        return request.getParameterValues(string);
    }

    @Override
    public String getSessionId() {
        return request.getSession().getId();
    }

    @Override
    public T getModelObject() {
        return (T)request.getSession().getAttribute(HttpSessionAttributes.MODELOBJECT);
    }

    @Override
    public Map<String, MultipartFile> getFiles() {
        return files;
    }

    @Override
    public Map<String, List<MultipartFile>> getMultiValueFiles() {
        return this.multiValueFiles;
    }

    @Override
    public AttributeService getAttributeService() {
        return attributeService;
    }
}
