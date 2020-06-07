/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.looseboxes.webform.form;

import com.bc.webform.Form;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author hp
 */
public interface DependentsUpdater {

    Form<Object> update(
            Form<Object> form, Class memberType, 
            List memberEntities, Locale locale);
}
