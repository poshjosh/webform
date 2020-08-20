package com.looseboxes.webform.store;

import com.bc.webform.form.Form;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigDTO;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class FormConfigStore {
    
    private static final Logger LOG = LoggerFactory.getLogger(FormConfigStore.class);
    
    private final AttributeStore store;

    public FormConfigStore(AttributeStore store) {
        this.store = Objects.requireNonNull(store);
    }
    
    public Form getFormOrException(String formid) {
        
        Objects.requireNonNull(formid);
        
        final FormConfig formConfig = getOrException(formid);
        
        final Form form = formConfig.getForm();
        
        if(form == null) {
            throw new InvalidRouteException("Form not found for: " + formConfig);
        }
    
        LOG.trace("For id: {}, found: {}", formid, formConfig);
        
        return form;
    }  
    
    public FormConfigDTO getOrException(String formid) {
        
        Objects.requireNonNull(formid);
        
        final FormConfigDTO formConfig = getOrDefault(formid, null);
        
        if(formConfig == null) {
            throw new InvalidRouteException("FormConfig not found for ID: " + formid);
        }
        
        return formConfig;
    }

    public FormConfigDTO getOrDefault(String formid, FormConfigDTO resultIfNone) {
        final String attributeName = formid;
        Objects.requireNonNull(attributeName);
        final Object value = this.getStore().getOrDefault(attributeName, null);
        LOG.trace("Got {} = {}", attributeName, value);
        return value == null ? resultIfNone : (FormConfigDTO)value;
    }

    public void remove(String formid) {
        Objects.requireNonNull(formid);
        final String name = formid;
        final Object removed = this.getStore().remove(name);  
        LOG.trace("Removed {} = {}", name, removed);
    }
    
    public void set(FormConfig formConfig){
        final String attributeName = formConfig.getFid();
        Objects.requireNonNull(attributeName);
        this.getStore().put(attributeName, formConfig);
        LOG.trace("Set {} = {}", attributeName, formConfig);
    }

    public AttributeStore getStore() {
        return store;
    }
}
