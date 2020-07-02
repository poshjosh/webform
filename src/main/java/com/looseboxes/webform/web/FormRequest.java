package com.looseboxes.webform.web;

import com.looseboxes.webform.services.FormAttributeService;

/**
 * @author hp
 */
public interface FormRequest<T> extends WebRequest<T>{
    
    default FormRequest formConfig(FormConfigBean formConfig) {
        setFormConfig(formConfig);
        return this;
    }
    
    FormRequest<T> copy();
    
    FormConfigBean getFormConfig();
    
    void setFormConfig(FormConfigBean formConfig);

    @Override
    FormAttributeService getAttributeService();
}
