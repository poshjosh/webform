package com.looseboxes.webform.web;

import com.looseboxes.webform.services.FormAttributeService;

/**
 * @author hp
 */
public interface FormRequest<T> extends WebRequest<T>{
    
    default FormRequest formConfig(FormConfigDTO formConfig) {
        setFormConfig(formConfig);
        return this;
    }
    
    FormRequest<T> copy();
    
    FormConfigDTO getFormConfig();
    
    void setFormConfig(FormConfigDTO formConfig);

    @Override
    FormAttributeService getAttributeService();
}
