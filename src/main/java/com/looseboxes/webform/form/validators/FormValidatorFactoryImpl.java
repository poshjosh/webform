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

import com.looseboxes.webform.form.FormRequestParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.validation.Validator;
import static com.looseboxes.webform.CrudAction.create;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 9:49:31 PM
 */
public class FormValidatorFactoryImpl implements FormValidatorFactory {

    private final EntityUniqueColumnsValidator uniqueColumnsValidator;

    public FormValidatorFactoryImpl(EntityUniqueColumnsValidator uniqueColumnsValidator) {
        this.uniqueColumnsValidator = Objects.requireNonNull(uniqueColumnsValidator);
    }
    
    @Override
    public List<Validator> get(FormRequestParams formReqParams) {
        
        final List<Validator> output = new ArrayList<>(2);
        
        if(create.equals(formReqParams.getAction())) {
            
            final Object modelobject = formReqParams.getModelobject();
            
            if(this.uniqueColumnsValidator.supports(modelobject.getClass())) {

                output.add(uniqueColumnsValidator);
            }
            
//            if(isTestCoordinatorsForm.test(formReqParams)) {
//                final Validator validator = new NonEmptyCollectionValidator(Test_.userList.getName());
//                output.add(validator);
//            }
        }
        
        return Collections.unmodifiableList(output);
    }
}
