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

package com.looseboxes.webform.form.validators;

import java.util.List;
import org.springframework.validation.Validator;
import com.looseboxes.webform.form.FormConfig;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 9:48:35 PM
 */
public interface FormValidatorFactory {

    List<Validator> getValidators(FormConfig formConfig, Class domainType);
}
