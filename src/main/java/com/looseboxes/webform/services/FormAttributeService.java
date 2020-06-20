package com.looseboxes.webform.services;

import com.bc.webform.Form;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.Wrapper;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.form.FormConfig;
import com.looseboxes.webform.store.AttributeStore;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hp
 */
@Service
public class FormAttributeService implements Wrapper<StoreDelegate, FormAttributeService>{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormService.class);

    private final AttributeService attributeService;

    @Autowired
    public FormAttributeService(AttributeService attributeService) {
        this.attributeService = Objects.requireNonNull(attributeService);
    }

    @Override
    public FormAttributeService wrap(StoreDelegate delegate) {
        return new FormAttributeService(this.attributeService.wrap(delegate));
    }

    @Override
    public StoreDelegate unwrap() {
        return this.attributeService.unwrap();
    }
    
    public Form getFormOrException(String formid) {
        
        Objects.requireNonNull(formid);
        
        final FormConfig formConfig = getSessionAttributeOrException(formid);
        
        final Form form = formConfig.getForm();
        
        if(form == null) {
            throw new InvalidRouteException();
        }
    
        LOG.trace("For id: {}, found: {}", formid, formConfig);
        
        return form;
    }  
    
    public FormConfig getSessionAttributeOrException(String formid) {
        
        Objects.requireNonNull(formid);
        
        final FormConfig formConfig = getSessionAttribute(formid, null);
        
        if(formConfig == null) {
            throw new InvalidRouteException("FormConfig not found for: " + formid);
        }
        
        return formConfig;
    }

    public FormConfig getSessionAttribute(
            String formid, FormConfig resultIfNone) {
        final String attributeName = this.getAttributeName(formid);
        Objects.requireNonNull(attributeName);
        final Object value = this.sessionAttributes()
                .getOrDefault(attributeName, null);
        LOG.trace("Got {} = {}", attributeName, value);
        return value == null ? resultIfNone : (FormConfig)value;
    }

    public void removeSessionAttribute(String formid) {
        Objects.requireNonNull(formid);
        final String name = getAttributeName(formid);
        final Object removed = sessionAttributes().remove(name);  
        LOG.trace("Removed {} = {}", name, removed);
    }
    
    public void setSessionAttribute(FormConfig formConfig){
        final String attributeName = this.getAttributeName(formConfig);
        Objects.requireNonNull(attributeName);
        this.sessionAttributes().put(attributeName, formConfig);
        LOG.trace("Set {} = {}", attributeName, formConfig);
    }

    public String getAttributeName(FormConfig formConfig) {
        return this.getAttributeName(formConfig.getFormid());
    }
    
    public String getAttributeName(String formid) {
        Objects.requireNonNull(formid);
        return HttpSessionAttributes.formReqParams(formid);
    }
    
    public AttributeStore<HttpSession> sessionAttributes() {
        return this.attributeService.sessionAttributes();
    }

    public AttributeService getAttributeService() {
        return attributeService;
    }
}
