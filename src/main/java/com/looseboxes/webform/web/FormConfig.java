/*
 * Copyright 2019 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.looseboxes.webform.web;

import com.bc.webform.form.Form;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.looseboxes.webform.CRUDAction;
import static com.looseboxes.webform.HttpSessionAttributes.FORM;
import com.looseboxes.webform.Params;
import java.util.List;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:16:30 PM
 */
public interface FormConfig{
    
    static String [] names() {
        final String [] paramNames = Params.names();
        final String [] allNames = new String[paramNames.length + 1];
        System.arraycopy(paramNames, 0, allNames, 0, paramNames.length);
        allNames[paramNames.length] = FORM;
        return allNames;
    }

    class Builder extends FormConfigBean{}
    
    FormConfig copy();
    
    FormConfigBean writableCopy();
    
    /**
     * Alias for {@link #getFormid()}
     * @return String. The id of the respective form for this config
     * @see #getFormid() 
     */
    String getFid();

    /**
     * Alias for {@link #getParentFormid()}
     * @return String. The id of the respective parent form of the form for this config
     * @see #getParentFormid() 
     */
    String getParentfid();

    /**
     * Alias for {@link #getModelid()}
     * @return String. The id of the model which the form of this config relates to
     * @see #getModelid() 
     */
    String getId();
    
    /**
     * @return String. The name of the enum returned by {@link #getCrudAction()}
     * @see #getCrudAction() 
     */
    String getAction();
    
    @JsonIgnore
    CRUDAction getCrudAction();

    List<String> getModelfields();
    
    /**
     * Alias for {@link #getParentfid()}
     * @return the id of the parent form of this formConfig's form
     * @see #getParentfid() 
     */
    @JsonIgnore
    String getParentFormid();
    
    /**
     * Alias for {@link #getFid()}
     * @return the id of this formConfig's form
     * @see #getFid() 
     */
    @JsonIgnore
    String getFormid();

    /**
     * Alias for {@link #getId()}
     * @return the id of the model object for which a form will be displayed
     * @see #getId() 
     */
    @JsonIgnore
    String getModelid();

    String getModelname();

    @JsonIgnore
    default Object getModelobject() {
        final Form form = this.getForm();
        return form == null ? null : form.getDataSource();
    }
    
    String getTargetOnCompletion();
    
    Form<Object> getForm();
    
    Map<String, Object> toMap();
}
