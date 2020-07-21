package com.looseboxes.webform.services;

import com.bc.webform.form.Form;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.web.FormConfig;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.store.AttributeStoreProvider;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hp
 */
@Service
public class FormAttributeService extends AttributeService{
    
    private static final Logger LOG = LoggerFactory.getLogger(ModelObjectService.class);

    @Autowired
    public FormAttributeService(AttributeStoreProvider provider) {
        super(provider);
    }

    public FormAttributeService(AttributeStoreProvider provider, StoreDelegate delegate) {
        super(provider, delegate);
    }

    @Override
    public FormAttributeService wrap(StoreDelegate delegate) {
        return new FormAttributeService(this.getAttributeStoreProvider(), delegate);
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
    
    public FormConfigDTO getSessionAttributeOrException(String formid) {
        
        Objects.requireNonNull(formid);
        
        final FormConfigDTO formConfig = getSessionAttribute(formid, null);
        
        if(formConfig == null) {
            throw new InvalidRouteException("FormConfig not found for: " + formid);
        }
        
        return formConfig;
    }

    public FormConfigDTO getSessionAttribute(
            String formid, FormConfigDTO resultIfNone) {
        final String attributeName = formid;
        Objects.requireNonNull(attributeName);
        final Object value = this.sessionAttributes()
                .getOrDefault(attributeName, null);
        LOG.trace("Got {} = {}", attributeName, value);
        return value == null ? resultIfNone : (FormConfigDTO)value;
    }

    public void removeSessionAttribute(String formid) {
        Objects.requireNonNull(formid);
        final String name = formid;
        final Object removed = sessionAttributes().remove(name);  
        LOG.trace("Removed {} = {}", name, removed);
    }
    
    public void setSessionAttribute(FormConfig formConfig){
        final String attributeName = formConfig.getFid();
        Objects.requireNonNull(attributeName);
        this.sessionAttributes().put(attributeName, formConfig);
        LOG.trace("Set {} = {}", attributeName, formConfig);
    }
}
