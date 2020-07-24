package com.looseboxes.webform.web;

import com.looseboxes.webform.Errors;
import com.looseboxes.webform.FormEndpoints;
import com.looseboxes.webform.FormStage;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;

/**
 * @author hp
 */
public class HtmResponseHandler implements ResponseHandler<FormConfigDTO, String>{
    
    private static final Logger LOG = LoggerFactory.getLogger(HtmResponseHandler.class);
    
    private final FormEndpoints formEndpoints;

    public HtmResponseHandler(FormEndpoints formEndpoints) {
        this.formEndpoints = Objects.requireNonNull(formEndpoints);
    }

    @Override
    public String respond(FormConfigDTO formConfig) {
        final FormStage formStage = formConfig.getFormStage();
        if(FormStage.isLast(formStage)) {
            formConfig.addInfo("Success");
        }
        final String result;
        switch(formStage) {
            case BEGIN: result = formEndpoints.forCrudAction(formConfig.getCrudAction()); break;
            case VALIDATE: result = getTargetAfterValidate(formConfig.getBindingResult()); break;
            case SUBMIT: result = getTargetAfterSubmit(formConfig); break;
            default: throw Errors.unexpectedElement(formStage, FormStage.values());
        }
        return result;
    }

    @Override
    public String respond(FormConfigDTO formConfig, Exception e) {
        final String err = "An unexpected error occured";
        LOG.warn(err, e);
        if(formConfig != null) {
            formConfig.addError(err);
        }
        return formEndpoints.getError();
    }
    
    private String getTargetAfterValidate(BindingResult bindingResult) {
        
        final String target;
        
        if (bindingResult.hasErrors()) {
            
            target = formEndpoints.getForm();
            
        }else{
            
            target = formEndpoints.getFormConfirmation();
        }
        
        return target;
    }

    private String getTargetAfterSubmit(FormConfigDTO config) {
        
        final String target = config.getRedirectForTargetOnCompletion()
                .orElse(formEndpoints.getSuccess());
        
        LOG.debug("Target: {}", target);
        
        return target;
    } 
}
