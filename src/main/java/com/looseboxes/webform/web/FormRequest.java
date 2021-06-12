package com.looseboxes.webform.web;

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
}
