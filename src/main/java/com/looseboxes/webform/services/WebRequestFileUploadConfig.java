package com.looseboxes.webform.services;

import com.looseboxes.webform.HttpSessionAttributes;
import javax.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author hp
 */
public class WebRequestFileUploadConfig extends FileUploadConfig<Object>{
    
    public static FileUploadConfig validInstanceOrNull(@Nullable HttpServletRequest request) {
        if(request instanceof MultipartHttpServletRequest) {
            return new WebRequestFileUploadConfig((MultipartHttpServletRequest)request);
        }else{
            return null;
        }
    }

    public WebRequestFileUploadConfig(MultipartHttpServletRequest request) {
        this.setFiles(request.getFileMap());
        this.setId(request.getSession().getId());
        this.setModelobject(request.getSession().getAttribute(HttpSessionAttributes.MODELOBJECT));
        this.setMultiValueFiles(request.getMultiFileMap());
    }
}
