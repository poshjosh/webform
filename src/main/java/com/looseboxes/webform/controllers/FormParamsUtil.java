package com.looseboxes.webform.controllers;

import com.looseboxes.webform.Params;
import com.looseboxes.webform.form.FormConfigBean;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;

/**
 * @author hp
 */
public final class FormParamsUtil {
    
    private static final Logger LOG = LoggerFactory.getLogger(FormParamsUtil.class);
    
    private FormParamsUtil() { }
    
    /**
     * Update the FormConfigBean with form related parameters from the request.
     * 
     * The FormConfigBean passed by Spring to the controller methods was not
     * being updated with parameters from query e.g <code>?user=jane&age=23</code>
     * 
     * This method manually updates those parameters in the FormConfigBean
     * @param formConfig
     * @param request 
     */
    public static void updateFormConfigWithFormParamsFromRequest(
            FormConfigBean formConfig, HttpServletRequest request) {
        LOG.trace("BEFORE: {}\nHttpServletRequest.queryString: {}", 
                formConfig, request.getQueryString());
        final String [] names = Params.names();
        for(String name : names) {
            final Object value = getParameter(request, name);
            if(value != null) {
                formConfig.setIfAbsent(name, value);
            }
        }
        LOG.debug(" AFTER: {}", formConfig);
    }

    public static void updateModelMapWithFormParamsFromRequest(
            ModelMap model, HttpServletRequest request) {
        LOG.trace("BEFORE: {}\nHttpServletRequest.queryString: {}", 
                model, request.getQueryString());
        final String [] names = Params.names();
        for(String name : names) {
            final Object value = getParameter(request, name);
            if(value != null) {
                model.putIfAbsent(name, value);
            }
        }
        LOG.debug(" AFTER: {}", model);
    }
    
    private static Object getParameter(HttpServletRequest request, String name) {
        final Object value;
        if(Params.isMultiValue(name)) {
            value = request.getParameterValues(name);
        }else{
            value = getSingleParameterOrNull(request, name);
        }
        LOG.trace("HttpServletRequest parameter: {} = {}", name, value);
        return value;
    }
    
    private static String getSingleParameterOrNull(HttpServletRequest request, String name){
        final String param = request.getParameter(name);
        return param == null || param.isEmpty() ? null : param;
    }
}
