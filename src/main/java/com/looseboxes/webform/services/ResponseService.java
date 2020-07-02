package com.looseboxes.webform.services;

import com.looseboxes.webform.web.FormConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

/**
 *
 * @author hp
 */
public interface ResponseService<T> {

    ResponseEntity<T> respond(FormConfig formConfig);

    ResponseEntity<Object> respond(BindingResult bindingResult, ModelMap model);

    ResponseEntity<Object> respond(BindingResult bindingResult, String propertyName, ModelMap model);

    ResponseEntity<Object> respond(BindingResult bindingResult, ModelMap model, Object payload);

    ResponseEntity<Object> respond(BindingResult bindingResult, String propertyName, ModelMap model, Object payload);

    ResponseEntity<Object> respond(Exception e, ModelMap model);
    
}
