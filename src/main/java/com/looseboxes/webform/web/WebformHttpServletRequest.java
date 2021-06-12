package com.looseboxes.webform.web;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hp
 */
public class WebformHttpServletRequest extends WebHttpServletRequest implements FormRequest{
    
    private FormConfigDTO formConfig;

    public WebformHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public WebformHttpServletRequest copy() {
        return new WebformHttpServletRequest(getHttpServletRequest());
    }
    
    @Override
    public WebformHttpServletRequest formConfig(FormConfigDTO formConfig) {
        this.setFormConfig(formConfig);
        return this;
    }

    @Override
    public FormConfigDTO getFormConfig() {
        return formConfig;
    }

    @Override
    public void setFormConfig(FormConfigDTO formConfig) {
        this.formConfig = formConfig;
    }
}