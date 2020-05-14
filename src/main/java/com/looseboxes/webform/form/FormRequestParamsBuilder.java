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
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.ModelAttributes;
import com.looseboxes.webform.Params;
import io.micrometer.core.lang.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:11:48 PM
 */
public class FormRequestParamsBuilder implements Serializable, FormRequestParams {

    private String action;
    private String modelname;
    private String formid;
    private String modelid;
    private Object modelobject;
    
    private List<String> modelfields = Collections.EMPTY_LIST;
    
    @Nullable private Form form;
    
    private boolean buildAttempted;

    public FormRequestParamsBuilder() { }
    
    public FormRequestParams build() {

        if(buildAttempted) {
            throw Errors.methodAlreadyCalled("build()");
        }
        
        buildAttempted = true;
        
        Objects.requireNonNull(action);
        Objects.requireNonNull(modelname);
        Objects.requireNonNull(formid);
        
        return this;
    }
    
    public FormRequestParamsBuilder action(String arg) {
        this.action = arg;
        return this;
    }

    public FormRequestParamsBuilder modelname(String arg) {
        this.modelname = arg;
        return this;
    }

    public FormRequestParamsBuilder formid(String arg) {
        this.formid = arg;
        return this;
    }
    
    public FormRequestParamsBuilder modelid(String arg) {
        this.modelid = arg;
        return this;
    }

    public FormRequestParamsBuilder modelobject(Object arg) {
        this.modelobject = arg;
        return this;
    }

    public FormRequestParamsBuilder modelfields(String arg) {
        return modelfields(Collections.singletonList(Objects.requireNonNull(arg)));
    }
    
    public FormRequestParamsBuilder modelfields(String... arg) {
        if(arg == null) {
            return modelfields(Collections.EMPTY_LIST);
        }else if(arg.length == 0) {
            return modelfields(Collections.EMPTY_LIST);
        }else{
            return modelfields(Arrays.asList(arg));
        }
    }
    public FormRequestParamsBuilder modelfields(List<String> arg) {
        this.modelfields = arg == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(arg);
        return this;
    }
    
    public FormRequestParamsBuilder form(Form form) {
        this.form = form;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>(7, 1.0f);
        map.put(Params.ACTION, getAction());
        map.put(Params.MODELNAME, getModelname());
        map.put(Params.FORMID, getFormid());
        map.put(Params.MODELID, getModelid());
        map.put(Params.MODELFIELDS, getModelfields());
        map.put(ModelAttributes.MODELOBJECT, getModelobject());
        this.getFormOptional().ifPresent((f) -> {
            map.put(ModelAttributes.FORM, f);
        });
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public String getModelname() {
        return modelname;
    }

    @Override
    public String getFormid() {
        return formid;
    }
    
    @Override
    public String getModelid() {
        return modelid;
    }

    @Override
    public Object getModelobject() {
        return modelobject;
    }

    @Override
    public List<String> getModelfields() {
        return modelfields;
    }

    @Override
    public Optional<Form> getFormOptional() {
        return Optional.ofNullable(form);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.action);
        hash = 41 * hash + Objects.hashCode(this.modelname);
        hash = 41 * hash + Objects.hash(this.formid);
        hash = 41 * hash + Objects.hashCode(this.modelid);
        hash = 41 * hash + Objects.hashCode(this.modelobject);
        hash = 41 * hash + Objects.hashCode(this.modelfields);
        hash = 41 * hash + Objects.hashCode(this.form);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FormRequestParamsBuilder other = (FormRequestParamsBuilder) obj;
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        if (!Objects.equals(this.modelname, other.modelname)) {
            return false;
        }
        if (!Objects.equals(this.formid, other.formid)) {
            return false;
        }
        if (!Objects.equals(this.modelid, other.modelid)) {
            return false;
        }
        if (!Objects.equals(this.modelobject, other.modelobject)) {
            return false;
        }
        if (!Objects.equals(this.modelfields, other.modelfields)) {
            return false;
        }
        if (!Objects.equals(this.form, other.form)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FormRequestParamsBuilder{" + "action=" + action + 
                ", modelname=" + modelname + ", formid=" + formid + 
                ", modelid=" + modelid + ", modelobject=" + modelobject + 
                ", modelfields=" + modelfields + ", form=" + form.getDisplayName() + '}';
    }
}
