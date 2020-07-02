package com.looseboxes.webform.web;

import com.looseboxes.webform.services.FormAttributeService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.ModelMap;

/**
 * @author hp
 */
public class WebformHttpServletRequest extends WebHttpServletRequest implements FormRequest{
    
    private FormConfigBean formConfig;

    public WebformHttpServletRequest(
            HttpServletRequest request, ModelMap modelMap, FormAttributeService attributeService) {
        super(request, modelMap, attributeService);
    }

    @Override
    public WebformHttpServletRequest copy() {
        return new WebformHttpServletRequest(
                getHttpServletRequest(), getModelMap(), getAttributeService());
    }

    @Override
    public FormAttributeService getAttributeService() {
        return (FormAttributeService)super.getAttributeService();
    }
    
    @Override
    public WebformHttpServletRequest formConfig(FormConfigBean formConfig) {
        this.setFormConfig(formConfig);
        return this;
    }

    @Override
    public FormConfigBean getFormConfig() {
        return formConfig;
    }

    @Override
    public void setFormConfig(FormConfigBean formConfig) {
        this.formConfig = formConfig;
    }
}