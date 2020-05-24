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
import com.looseboxes.webform.ModelAttributes;
import com.looseboxes.webform.Params;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:16:30 PM
 */
public interface FormRequestParams extends ModelAttributes, Params{
    
    static String [] names() {
        return new String[]{ACTION, MODELFIELDS, FORMID, MODELID, MODELNAME,
            MODELOBJECT, PARENT_FORMID, TARGET_ON_COMPLETION, FORM};
    }
    
    class Builder extends FormRequestParamsBuilder{}

    String getAction();

    List<String> getModelfields();
    
    String getParentFormid();
    
    String getFormid();

    String getModelid();

    String getModelname();

    Object getModelobject();
    
    String getTargetOnCompletion();
    
    Optional<Form<Object>> getFormOptional();
    
    Map<String, Object> toMap();
}
