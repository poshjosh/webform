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
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.Params;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:11:48 PM
 */
public class FormConfigDTO implements Serializable, FormConfig {

    @NotNull
    private String action;
    
    @NotNull
    private String modelname;
    
    private String parentfid;

    private String fid;

    private String mid;

    private Object modelobject;
    
    private List<String> modelfields = Collections.EMPTY_LIST;
    
    private String targetOnCompletion;
    
    private Form<Object> form;
    
    private boolean buildAttempted;

    public FormConfigDTO() { }
    
    public FormConfig build() {

        if(buildAttempted) {
            throw Errors.methodAlreadyCalled("build()");
        }
        
        buildAttempted = true;
        
        Objects.requireNonNull(action);
        Objects.requireNonNull(modelname);
        Objects.requireNonNull(fid);
        
        return this;
    }
    
    public void apply(Converter<String, String> converter) {
        this.action(converter.convert(this.action));
        this.modelname(converter.convert(this.modelname));
        this.parentfid(converter.convert(this.parentfid));
        this.fid(converter.convert(this.fid));
        this.mid(converter.convert(this.mid));
        this.targetOnCompletion(converter.convert(this.targetOnCompletion));
    }
    
    public FormConfigDTO with(FormConfig arg) {
        this.action(arg.getCrudAction());
        this.form(arg.getForm());
        this.formid(arg.getFormid());
        this.modelfields(arg.getModelfields() == null ? null :
                arg.getModelfields().isEmpty() ? Collections.EMPTY_LIST :
                Collections.unmodifiableList(arg.getModelfields()));
        this.modelid(arg.getModelid());
        this.modelname(arg.getModelname());
        this.modelobject(arg.getModelobject());
        this.parentFormid(arg.getParentFormid());
        this.targetOnCompletion(arg.getTargetOnCompletion());
        return this;
    }

    public FormConfigDTO action(CRUDAction arg) {
        return this.action(arg.name());
    }

    public FormConfigDTO action(String arg) {
        this.action = arg;
        return this;
    }
    
    public FormConfigDTO modelname(String arg) {
        this.modelname = arg;
        return this;
    }

    public FormConfigDTO parentFormid(String arg) {
        return this.parentfid(arg);
    }

    public FormConfigDTO parentfid(String arg) {
        this.parentfid = arg;
        return this;
    }

    public FormConfigDTO formid(String arg) {
        return this.fid(arg);
    }
    
    public FormConfigDTO fid(String arg) {
        this.fid = arg;
        return this;
    }
    
    public FormConfigDTO modelid(String arg) {
        return this.mid(arg);
    }

    public FormConfigDTO mid(String arg) {
        this.mid = arg;
        return this;
    }

    public FormConfigDTO modelobject(Object arg) {
        this.modelobject = arg;
        return this;
    }

    public FormConfigDTO modelfields(String arg) {
        return modelfields(Collections.singletonList(Objects.requireNonNull(arg)));
    }
    
    public FormConfigDTO modelfields(String... arg) {
        if(arg == null) {
            return modelfields(Collections.EMPTY_LIST);
        }else if(arg.length == 0) {
            return modelfields(Collections.EMPTY_LIST);
        }else{
            return modelfields(Arrays.asList(arg));
        }
    }
    public FormConfigDTO modelfields(List<String> arg) {
        this.modelfields = arg == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(arg);
        return this;
    }

    public FormConfigDTO targetOnCompletion(String target) {
        this.targetOnCompletion = target;
        return this;
    }
    
    public FormConfigDTO form(Form form) {
        this.form = form;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>(16, 0.75f);
        // We add action NOT crudAction
        map.put(Params.ACTION, getAction());
        map.put(Params.MODELNAME, getModelname());
        map.put(Params.PARENT_FORMID, getParentFormid());
        map.put(Params.FORMID, getFormid());
        map.put(Params.MODELID, getModelid());
        map.put(Params.MODELFIELDS, getModelfields());
        map.put(HttpSessionAttributes.MODELOBJECT, getModelobject());
        map.put(Params.TARGET_ON_COMPLETION, getTargetOnCompletion());
        map.put(HttpSessionAttributes.FORM, getForm());
        return Collections.unmodifiableMap(map);
    }
    
    @Override
    public String getAction() {
        return this.getCrudAction().name();
    }
    
    public void setAction(String crudAction) {
        this.setCrudAction(CRUDAction.valueOf(crudAction));
    }

    @Override
    public CRUDAction getCrudAction() {
        return CRUDAction.valueOf(action);
    }

    public void setCrudAction(CRUDAction crudAction) {
        this.action = crudAction.name();
    }

    @Override
    public String getModelname() {
        return modelname;
    }

    public void setModelname(String modelname) {
        this.modelname = modelname;
    }

    @Override
    public String getParentFormid() {
        return this.getParentfid();
    }

    public void setParentFormid(String parentFormid) {
        this.setParentfid(parentFormid);
    }

    @Override
    public String getParentfid() {
        return parentfid;
    }

    public void setParentfid(String parentFormid) {
        this.parentfid = parentFormid;
    }

    @Override
    public String getFormid() {
        return this.getFid();
    }

    public void setFormid(String formid) {
        this.setFid(formid);
    }

    @Override
    public String getFid() {
        return fid;
    }
    
    public void setFid(String formid) {
        this.fid = formid;
    }

    @Override
    public String getMid() {
        return mid;
    }
    
    public void setMid(String modelid) {
        this.mid = modelid;
    }
    
    @Override
    public String getModelid() {
        return this.getMid();
    }

    public void setModelid(String modelid) {
        this.setMid(modelid);
    }

    @Override
    public Object getModelobject() {
        return modelobject;
    }

    public void setModelobject(Object modelobject) {
        this.modelobject = modelobject;
    }

    @Override
    public List<String> getModelfields() {
        return modelfields;
    }

    public void setModelfields(List<String> modelfields) {
        this.modelfields = modelfields;
    }

    @Override
    public String getTargetOnCompletion() {
        return targetOnCompletion;
    }

    public void setTargetOnCompletion(String targetOnCompletion) {
        this.targetOnCompletion = targetOnCompletion;
    }

    public void setForm(Form<Object> form) {
        this.form = form;
    }

    @Override
    public Form<Object> getForm() {
        return this.form;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.action);
        hash = 41 * hash + Objects.hashCode(this.modelname);
        hash = 41 * hash + Objects.hash(this.parentfid);
        hash = 41 * hash + Objects.hash(this.fid);
        hash = 41 * hash + Objects.hashCode(this.mid);
        hash = 41 * hash + Objects.hashCode(this.modelobject);
        hash = 41 * hash + Objects.hashCode(this.modelfields);
        hash = 41 * hash + Objects.hash(this.targetOnCompletion);
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
        final FormConfigDTO other = (FormConfigDTO) obj;
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        if (!Objects.equals(this.modelname, other.modelname)) {
            return false;
        }
        if (!Objects.equals(this.parentfid, other.parentfid)) {
            return false;
        }
        if (!Objects.equals(this.fid, other.fid)) {
            return false;
        }
        if (!Objects.equals(this.mid, other.mid)) {
            return false;
        }
        if (!Objects.equals(this.modelobject, other.modelobject)) {
            return false;
        }
        if (!Objects.equals(this.modelfields, other.modelfields)) {
            return false;
        }
        if (!Objects.equals(this.targetOnCompletion, other.targetOnCompletion)) {
            return false;
        }
        if (!Objects.equals(this.form, other.form)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FormConfigDTO{" + "action=" + action +
                ", parentFormid=" + parentfid +
                ", modelname=" + modelname + ", formid=" + fid + 
                ", modelid=" + mid + ", modelobject=" + modelobject + 
                ", modelfields=" + modelfields + ", targetOnCompletion=" +
                targetOnCompletion + ", form=" + 
                (form == null ? null : form.getName()) + '}';
    }
}
