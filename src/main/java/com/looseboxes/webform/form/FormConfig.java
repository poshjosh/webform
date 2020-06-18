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

package com.looseboxes.webform.form;

import com.bc.webform.Form;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.HttpSessionAttributes;
import static com.looseboxes.webform.HttpSessionAttributes.FORM;
import static com.looseboxes.webform.HttpSessionAttributes.MODELOBJECT;
import com.looseboxes.webform.Params;
import java.util.List;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:16:30 PM
 */
public interface FormConfig extends HttpSessionAttributes, Params{
    
    static String [] names() {
        return new String[]{ACTION, MODELFIELDS, FORMID, MODELID, MODELNAME,
            MODELOBJECT, PARENT_FORMID, TARGET_ON_COMPLETION, FORM};
    }

    class Builder extends FormConfigBean{}
    
    FormConfig copy();
    
    FormConfigBean writableCopy();
    
    /**
     * Synonymous to {@link #getFormid()}
     * @return String. The id of the respective form for this config
     * @see #getFormid() 
     */
    String getFid();

    /**
     * Synonymous to {@link #getParentFormid()}
     * @return String. The id of the respective parent form of the form for this config
     * @see #getParentFormid() 
     */
    String getParentfid();

    /**
     * Synonymous to {@link #getModelid()}
     * @return String. The id of the model which the form of this config relates to
     * @see #getModelid() 
     */
    String getMid();
    
    /**
     * @return String. The name of the enum returned by {@link #getCrudAction()}
     * @see #getCrudAction() 
     */
    String getAction();
    
    CRUDAction getCrudAction();

    List<String> getModelfields();
    
    String getParentFormid();
    
    String getFormid();

    String getModelid();

    String getModelname();

    default Object getModelobject() {
        return this.getForm().getDataSource();
    }
    
    String getTargetOnCompletion();
    
    Form<Object> getForm();
    
    Map<String, Object> toMap();
}
