/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.looseboxes.webform.form;

import com.looseboxes.webform.exceptions.FormMemberNotFoundException;
import com.looseboxes.webform.web.FormConfigDTO;

/**
 *
 * @author hp
 */
public interface FormMemberUpdater {

    FormConfigDTO update(FormConfigDTO formConfig, String memberName, Object memberValue) 
            throws FormMemberNotFoundException;
}
