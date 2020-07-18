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

package com.looseboxes.webform;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 17, 2019 9:39:03 PM
 */
public interface Params {
    
    String ACTION = "action";
    String MODELNAME = "modelname";
    String FORMID = "fid";
    String MODELID = "id";
    String MODELFIELDS = "modelfields";
    String PARENT_FORMID = "parentfid";
    String TARGET_ON_COMPLETION = "targetOnCompletion";
    
    static boolean isMultiValue(String name) {
        return MODELFIELDS.equals(name);
    }
    
    static String [] names() {
        return new String[]{ACTION, MODELFIELDS, FORMID, MODELID, 
            MODELNAME, PARENT_FORMID, TARGET_ON_COMPLETION};
    }
}
