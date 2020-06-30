package com.looseboxes.webform.services;

import com.looseboxes.webform.web.FormConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author hp
 */
@Service
public class FormConfigResponseService extends AbstractResponseService{
    
    public FormConfigResponseService(
            MessageAttributesService messageAttributesService) {
        super(messageAttributesService);
    }
    
    @Override
    public ResponseEntity<FormConfig> respond(FormConfig formConfig) {
        if(formConfig == null) {
            return ResponseEntity.badRequest().build();
        }else{
            return ResponseEntity.ok(formConfig);
        }
    }
}
