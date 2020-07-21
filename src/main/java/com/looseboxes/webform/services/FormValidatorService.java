package com.looseboxes.webform.services;

import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.web.FormConfigDTO;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author hp
 */
@Service
public class FormValidatorService<T>{
    
    private final FormValidatorFactory formValidatorFactory;

    public FormValidatorService(FormValidatorFactory formValidatorFactory) {
        this.formValidatorFactory = Objects.requireNonNull(formValidatorFactory);
    }
    
    public void validateModelObject(FormConfigDTO formConfig) {
        
        final Object modelobject = Objects.requireNonNull(formConfig.getModelobject());
        final BindingResult bindingResult = Objects.requireNonNull(formConfig.getBindingResult());
    
        final List<Validator> validators = this.formValidatorFactory
                .getValidators(formConfig, modelobject.getClass());

        for(Validator validator : validators) {

            ValidationUtils.invokeValidator(validator, modelobject, bindingResult);
        }
    }
}
