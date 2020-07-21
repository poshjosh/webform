package com.looseboxes.webform.web;

import com.looseboxes.webform.FormStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author hp
 */
public class FormConfigResponseHandler implements ResponseHandler<FormConfigDTO, ResponseEntity<FormConfigDTO>>{
    
    private final Logger log = LoggerFactory.getLogger(FormConfigResponseHandler.class);
    
    @Override
    public ResponseEntity<FormConfigDTO> respond(FormConfigDTO formConfig) {
        if(formConfig == null) {
            log.warn("FormConfig == null, while preparing response");
            formConfig = new FormConfigDTO();
            formConfig.addError("An unexpected error occured");
        }
        if(formConfig.hasErrors()) {
            return ResponseEntity.badRequest().body(formConfig);
        }else{
            if(FormStage.isLast(formConfig.getFormStage())) {
                formConfig.addInfo("Success");
            }
            return ResponseEntity.ok(formConfig);
        }
    }
    
    @Override
    public ResponseEntity<FormConfigDTO> respond(FormConfigDTO formConfig, Exception e) {
        final String err = "An unexpected error occured";
        log.warn(err, e);
        if(formConfig == null) {
            formConfig = new FormConfigDTO();
        }
        formConfig.addError(err);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(formConfig);
    }
}
