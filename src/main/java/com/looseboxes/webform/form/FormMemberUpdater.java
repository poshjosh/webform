/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.looseboxes.webform.form;

import com.bc.webform.choices.SelectOption;
import com.bc.webform.form.FormBean;
import com.bc.webform.form.member.FormMemberBean;
import com.looseboxes.webform.exceptions.FormMemberNotFoundException;
import com.looseboxes.webform.web.FormConfigDTO;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author hp
 */
public interface FormMemberUpdater {

    FormConfigDTO setValue(FormConfigDTO formConfig, String memberName, Object memberValue) 
            throws FormMemberNotFoundException;
    
//    FormConfigDTO setChoices(
//            FormConfigDTO formConfig, String memberName, List<SelectOption> choices)
//            throws FormMemberNotFoundException;
}
