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
 * @author Chinomso Bassey Ikwuagwu on Apr 19, 2019 4:48:42 PM
 */
public interface Templates {
    String ERROR = "error"; 
    String FORM = "form"; 
    String FORM_CONFIRMATION = "formConfirmation"; 
    String FORM_DATA = "formData"; 
    String HOME = "home"; 
    String SUCCESS = "success"; 
    static String [] all() {
        return new String[]{ERROR, FORM, FORM_CONFIRMATION, FORM_DATA, HOME, SUCCESS};
    }
}
