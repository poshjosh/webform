package com.looseboxes.webform.controllers;

import com.looseboxes.webform.web.FormConfigDTO;
import org.slf4j.Logger;

/**
 * @author hp
 */
public final class FormConfigLogUtil {
    
    private FormConfigLogUtil() {}
    
    public static void logWith(Logger log, FormConfigDTO formConfig) {
        
        if(formConfig == null) {
            
            log.warn("FormConfig == null");
            
        }else{
            
            log.debug(
                    "{} [{}] /{}/{}?fid={}&parentfid={}&targetOnCompletion={}\n DataSource: {}", 
                    formConfig.getFormStage(), 
                    formConfig.getModelid() == null ? "-" : formConfig.getModelid(), 
                    formConfig.getAction(), 
                    formConfig.getModelname(), 
                    formConfig.getFid(), 
                    formConfig.getParentfid(), 
                    formConfig.getTargetOnCompletion(),
                    formConfig.getModelobject()
            );
            
            log.trace("Form: {}", formConfig.getForm());
            
            if(formConfig instanceof FormConfigDTO) {
                FormConfigDTO dto = (FormConfigDTO)formConfig;
                log.trace("Infos: {}, Errors: {}", dto.getInfos(), dto.getErrors());
            }
        }
    }
}
