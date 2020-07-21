package com.looseboxes.webform.web;

import com.looseboxes.webform.services.AttributeService;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

/**
 * @author hp
 */
public class WebHttpServletRequest<T> implements WebRequest<T>{
    
    private final HttpServletRequest httpServletRequest;
    
    private final Map<String, MultipartFile> files;

    private final Map<String, List<MultipartFile>> multiValueFiles;

    private final AttributeService attributeService;
    
    public WebHttpServletRequest(
            HttpServletRequest request, AttributeService attributeService) {
        this.httpServletRequest = Objects.requireNonNull(request);
        if(request instanceof MultipartRequest) {
            MultipartRequest multiPartRequest = (MultipartRequest)request;
            this.files = multiPartRequest.getFileMap();
            this.multiValueFiles = multiPartRequest.getMultiFileMap();
        }else{
            this.files = Collections.EMPTY_MAP;
            this.multiValueFiles = Collections.EMPTY_MAP;
        }
        this.attributeService = attributeService.wrap(new StoreDelegate(null, request));
    }

    @Override
    public boolean hasFiles() {
        return ! this.isNullEmpty(files) || ! this.isNullEmpty(this.multiValueFiles);
    }
    
    private boolean isNullEmpty(Map m) {
        return m == null || m.isEmpty();
    }

    @Override
    public String getParameter(String string) {
        return httpServletRequest.getParameter(string);
    }

    @Override
    public String[] getParameterValues(String string) {
        return httpServletRequest.getParameterValues(string);
    }

    @Override
    public String getSessionId() {
        return httpServletRequest.getSession().getId();
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

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }
}
