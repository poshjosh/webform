package com.looseboxes.webform.controllers;

import com.looseboxes.webform.Params;
import com.looseboxes.webform.web.FormConfigDTO;
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
     * This method manually updates those parameters in the FormConfigDTO
     * @param formConfig
     * @param request 
     */
    public static void updateFormConfigWithFormParamsFromRequest(
            FormConfigDTO formConfig, HttpServletRequest request) {
        
        LOG.trace("HttpServletRequest.queryString: {}", request.getQueryString());
        LOG.trace("BEFORE adding query parameters");
        FormConfigLogUtil.logWith(LOG, formConfig);
        
        final String [] names = Params.names();
        for(String name : names) {
            final Object value = getParameter(request, name);
            if(value != null) {
//                LOG.trace("Setting: {} to {}", name, value);
                formConfig.setIfAbsent(name, value);
            }
        }
        
        LOG.trace(" AFTER adding query parameters");
        FormConfigLogUtil.logWith(LOG, formConfig);
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
            final String [] values = request.getParameterValues(name);
            value = values;
//            LOG.trace("HttpServletRequest parameter: {} = {}", 
//                    name, (values==null?null:Arrays.toString(values)));
        }else{
            value = getSingleParameterOrNull(request, name);
//            LOG.trace("HttpServletRequest parameter: {} = {}", name, value);
        }
        return value;
    }
    
    private static String getSingleParameterOrNull(HttpServletRequest request, String name){
        final String param = request.getParameter(name);
        return param == null || param.isEmpty() ? null : param;
    }
}
