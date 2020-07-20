package com.looseboxes.webform.services;

import com.bc.webform.form.Form;
import com.bc.webform.form.FormBean;
import com.looseboxes.webform.TestConfig;
import com.looseboxes.webform.store.StoreDelegate;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigBean;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;

/**
 * @author hp
 */
public class FormAttributeServiceTest {

    @Test
    public void wrap_ShouldReturnANewInstance() {
        System.out.println("wrap_ShouldReturnANewInstance");
        ModelMap model = new ModelMap();
        StoreDelegate delegate = new StoreDelegate(model, null);
        FormAttributeService instance = this.getInstance();
        FormAttributeService result = instance.wrap(delegate);
        assertThat(result, not(instance));
    }

    @Test
    public void testGetFormOrException() {
        System.out.println("getFormOrException");
        String formid = this.getRandomFormId();
        FormAttributeService instance = this.getInstance();
        FormConfigBean formConfig = this.getFormConfigBean(formid);
        FormBean expResult = new FormBean();
        formConfig.setForm(expResult);
        instance.setSessionAttribute(formConfig);
        Form result = instance.getFormOrException(formid);
        assertThat(result, is(expResult));
    }

    @Test
    public void testRemoveSessionAttribute() {
        System.out.println("removeSessionAttribute");
        FormConfig formConfig = this.getFormConfigBean();
        FormAttributeService instance = this.getInstance();
        instance.setSessionAttribute(formConfig);
        assertThat(instance.getSessionAttribute(formConfig.getFormid(), null), is(formConfig));
        instance.removeSessionAttribute(formConfig.getFormid());
        assertThat(instance.getSessionAttribute(formConfig.getFormid(), null), nullValue());
    }
    
    @Test
    public void getSessionAttribute_ShouldReturnPreviouslySetAttribute() {
        System.out.println("getSessionAttribute_ShouldReturnPreviouslySetAttribute");
        FormConfig expResult = this.getFormConfigBean();
        FormConfig result = this.getSessionAttribute_whenCalledAfterSetSessionAttribute(expResult);
        assertThat(result, is(expResult));
    }
    
    public FormConfig getSessionAttribute_whenCalledAfterSetSessionAttribute(FormConfig formConfig) {
        FormAttributeService instance = this.getInstance();
        instance.setSessionAttribute(formConfig);
        return instance.getSessionAttribute(formConfig.getFormid(), null);
    }

    @Test
    public void testSetSessionAttribute() {
        System.out.println("setSessionAttribute");
        FormConfig formConfig = this.getFormConfigBean();
        FormAttributeService instance = this.getInstance();
        instance.setSessionAttribute(formConfig);
        FormConfig result = instance.getSessionAttribute(formConfig.getFormid(), null);
        assertThat(result, is(formConfig));
    }
    
    public FormConfigBean getFormConfigBean() {
        return new FormConfigBean().formid(this.getRandomFormId());
    }
    
    public String getRandomFormId() {
        return "form" + Long.toHexString(System.currentTimeMillis());
    }

    public FormConfigBean getFormConfigBean(String formid) {
        return new FormConfigBean().formid(formid);
    }
    
    public FormAttributeService getInstance() {
        ModelMap model = new ModelMap();
        MockHttpServletRequest request = new MockHttpServletRequest();
        StoreDelegate storeDelegate = new StoreDelegate(model, request);
        FormAttributeService instance = new TestConfig().getFormAttributeService();
        return instance.wrap(storeDelegate);
    }
}
