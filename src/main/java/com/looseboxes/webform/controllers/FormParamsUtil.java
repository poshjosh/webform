package com.looseboxes.webform.controllers;

import com.looseboxes.webform.Params;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        if(LOG.isTraceEnabled()) {
            LOG.trace("HttpServletRequest.parameterNames: {}", Collections.list(request.getParameterNames()));
            LOG.trace("BEFORE adding request parameters");
            LOG.trace("{}", formConfig.print());
        }

        final String [] names = Params.names();
        final List<String> attempted = new ArrayList<>(names.length);
        for(String name : names) {
            final Object value = getParameter(request, name);
            if(value != null) {
                attempted.add(name);
//                LOG.trace("Setting: {} to {}", name, value);
                formConfig.setIfAbsent(name, value);
            }
        }

////////// This is a temporary fix for issue #3 ////////////////////////////////
// We try to prevent fid from 
        if(isCandidateForBugFixOfIssue3(attempted, formConfig)) {
            LOG.debug("Applying temporary fix of issue #3 by deleting wrongly set form id");
            formConfig.setFid(null);
        }
////////////////////////////////////////////////////////////////////////////////        

        if(LOG.isTraceEnabled()) {
            LOG.trace(" AFTER adding request parameters");
            LOG.trace("{}", formConfig.print());
        }
    }

    private static boolean isCandidateForBugFixOfIssue3(List<String> names, FormConfig formConfig) {
        return (names.contains(Params.TARGET_ON_COMPLETION) &&
                names.contains(Params.FORMID) && names.contains(Params.PARENT_FORMID) &&
                formConfig.getFid() != null && formConfig.getFid().equals(formConfig.getParentFormid()));
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
