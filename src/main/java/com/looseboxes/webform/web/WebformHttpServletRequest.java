package com.looseboxes.webform.web;

import com.looseboxes.webform.services.FormAttributeService;
import javax.servlet.http.HttpServletRequest;

/**
 * @author hp
 */
public class WebformHttpServletRequest extends WebHttpServletRequest implements FormRequest{
    
    private FormConfigDTO formConfig;

    public WebformHttpServletRequest(
            HttpServletRequest request, FormAttributeService attributeService) {
        super(request, attributeService);
    }

    @Override
    public WebformHttpServletRequest copy() {
        return new WebformHttpServletRequest(
                getHttpServletRequest(), getAttributeService());
    }

    @Override
    public FormAttributeService getAttributeService() {
        return (FormAttributeService)super.getAttributeService();
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