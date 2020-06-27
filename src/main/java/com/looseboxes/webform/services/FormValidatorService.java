package com.looseboxes.webform.services;

import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.services.MessageAttributesService;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author hp
 */
@Service
public class FormValidatorService<T>{
    
    @Autowired private FormValidatorFactory formValidatorFactory;
    @Autowired private MessageAttributesService messageAttributesService;
    
    public void validateModelObject(
            BindingResult bindingResult, ModelMap model, FormConfig formConfig) {
        
        final T modelobject = (T)Objects.requireNonNull(formConfig.getModelobject());
        
        this.validateModelObject(bindingResult, model, formConfig, modelobject);
    }

    public void validateModelObject(
            BindingResult bindingResult, ModelMap model,
            FormConfig formConfig, T modelobject) {
        
        Objects.requireNonNull(bindingResult);
        Objects.requireNonNull(model);
        Objects.requireNonNull(formConfig);
        Objects.requireNonNull(modelobject);
    
        final List<Validator> validators = this.formValidatorFactory
                .getValidators(formConfig, modelobject.getClass());

        for(Validator validator : validators) {

            ValidationUtils.invokeValidator(validator, modelobject, bindingResult);
        }
        
        if (bindingResult.hasErrors()) {
            
            this.messageAttributesService.addErrorsToModel(bindingResult, model);
        }
    }
}
