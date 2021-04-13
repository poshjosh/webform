package com.looseboxes.webform.controllers;

import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigDTO;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

/**
 * @author hp
 */
public final class FormConfigLogUtil {
    
    private FormConfigLogUtil() {}
    
    public static void logWith(Logger log, FormConfig formConfig) {
        
        if(formConfig == null) {
            
            log.trace("FormConfig == null");
            
        }else{
            
            Map map = new HashMap(formConfig.toMap());
            map.remove("form");
            log.trace("FormConfig map: {}", map);
            log.trace("Form: {}", formConfig.getForm());
            log.trace("Model: {}", formConfig.getModelobject());
            
            if(formConfig instanceof FormConfigDTO) {
                FormConfigDTO dto = (FormConfigDTO)formConfig;
                log.trace("Infos: {}, Errors: {}", dto.getInfos(), dto.getErrors());
            }
        }
    }
}
