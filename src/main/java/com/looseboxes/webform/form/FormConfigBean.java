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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:11:48 PM
 */
public class FormConfigBean implements Serializable, FormConfig, Params {

    @NotNull
    private String action;
    
    @NotNull
    private String modelname;
    
    private String parentfid;

    private String fid;

    private String mid;

    private List<String> modelfields;
    
    private String targetOnCompletion;
    
    private Form<Object> form;
    
    private boolean buildAttempted;

    public FormConfigBean() { }
    
    public FormConfigBean(FormConfig form) { 
        this.init(form);
    }
    
    private void init(FormConfig arg) {
        this.action(arg.getCrudAction());
        this.form(arg.getForm());
        this.formid(arg.getFormid());
        this.modelfields(arg.getModelfields() == null ? null :
                arg.getModelfields().isEmpty() ? Collections.EMPTY_LIST :
                Collections.unmodifiableList(arg.getModelfields()));
        this.modelid(arg.getModelid());
        this.modelname(arg.getModelname());
        this.parentFormid(arg.getParentFormid());
        this.targetOnCompletion(arg.getTargetOnCompletion());
    }

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

    @Override
    public FormConfig copy() {
        return this.writableCopy();
    }

    @Override
    public FormConfigBean writableCopy() {
        return new FormConfigBean(this);
    }
    
    public FormConfigBean with(FormConfig arg) {
        this.init(arg);
        return this;
    }

    public FormConfigBean action(CRUDAction arg) {
        return this.action(arg.name());
    }

    public FormConfigBean action(String arg) {
        this.action = arg;
        return this;
    }
    
    public FormConfigBean modelname(String arg) {
        this.modelname = arg;
        return this;
    }

    public FormConfigBean parentFormid(String arg) {
        return this.parentfid(arg);
    }

    public FormConfigBean parentfid(String arg) {
        this.parentfid = arg;
        return this;
    }

    public FormConfigBean formid(String arg) {
        return this.fid(arg);
    }
    
    public FormConfigBean fid(String arg) {
        this.fid = arg;
        return this;
    }
    
    public FormConfigBean modelid(String arg) {
        return this.mid(arg);
    }

    public FormConfigBean mid(String arg) {
        this.mid = arg;
        return this;
    }

    public FormConfigBean modelfields(String... args) {
        if(args == null) {
            return modelfields((Collection)null);
        }else if(args.length == 0) {
            return modelfields(Collections.EMPTY_LIST);
        }else if(args.length == 1) {
            return modelfields(Collections.singletonList(args[0]));
        }else{
            return modelfields(Arrays.asList(args));
        }
    }
    public FormConfigBean modelfields(Collection<String> arg) {
        if(arg == null) {
            this.modelfields = null;
        }else if(arg.isEmpty()){    
            this.modelfields = Collections.EMPTY_LIST;
        }else if(arg.size() == 1) {
            this.modelfields = Collections.singletonList(arg.iterator().next());
        }else{
            this.modelfields = Arrays.asList(arg.toArray(new String[0]));
        }
        return this;
    }

    public FormConfigBean targetOnCompletion(String target) {
        this.targetOnCompletion = target;
        return this;
    }
    
    public FormConfigBean form(Form form) {
        this.form = form;
        return this;
    }
    
    public FormConfig setAllIfAbsent(Map<String, Object> map) {
        map.forEach((k, v) -> {
            this.setObject(k, v, false);
        });
        return this;
    }
    
    public FormConfig setAll(Map<String, Object> map) {
        map.forEach((k, v) -> {
            this.setObject(k, v, true);
        });
        return this;
    }

    public FormConfig setIfAbsent(String name, Object value) {
        return this.setObject(name, value, false);
    }

    public FormConfig set(String name, Object value) {
        return this.setObject(name, value, true);
    }
    
    private FormConfig setObject(String name, Object value, boolean overwrite) {
        switch(name) {
            case MODELFIELDS: 
                if(value instanceof Collection) {
                    return this.modelfields((Collection<String>)value);
                }else if(value instanceof String[]){
                    return this.modelfields((String[])value);
                }else{
                    if(value == null){
                        return this.modelfields((String[])value);
                    }else{
                        return this.modelfields(value.toString());
                    }
                }
            case HttpSessionAttributes.FORM: 
                return this.form(form == null || overwrite ? (Form)value : form);
            default: 
                return setString(name, value == null ? (String)value : value.toString(), overwrite);
        }
    }

    public FormConfig setIfAbsent(String name, String value) {
        return this.setString(name, value, false);
    }
    
    public FormConfig set(String name, String value) {
        return this.setString(name, value, true);
    }

    private FormConfig setString(String name, String value, boolean overwrite) {
        switch(name) {
            case ACTION: 
                return this.action((action == null || overwrite) ? value : action);
            case MODELNAME: 
                return this.modelname((modelname == null || overwrite) ? value : modelname);
            case PARENT_FORMID: 
                return this.parentfid((parentfid == null || overwrite) ? value : parentfid);
            case FORMID: 
                return this.fid((fid == null || overwrite) ? value : fid);
            case MODELID: 
                return this.mid((mid == null || overwrite) ? value : mid);
            case TARGET_ON_COMPLETION: 
                return this.targetOnCompletion(
                        (targetOnCompletion == null || overwrite) ? value : targetOnCompletion);
            default: throw new IllegalArgumentException(
                    "Parameter named: " + name + ", not found for: " + this);
        }
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>(8, 1.0f);
        // We add action NOT crudAction
        map.put(Params.ACTION, getAction());
        map.put(Params.MODELNAME, getModelname());
        map.put(Params.PARENT_FORMID, getParentFormid());
        map.put(Params.FORMID, getFormid());
        map.put(Params.MODELID, getModelid());
        map.put(Params.MODELFIELDS, getModelfields());
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
        final FormConfigBean other = (FormConfigBean) obj;
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
        return "FormConfigBean{" + "action=" + action +
                ", parentFormid=" + parentfid +
                ", modelname=" + modelname + ", formid=" + fid + 
                ", modelfields=" + modelfields + ", targetOnCompletion=" +
                targetOnCompletion + ", form=" + 
                (form == null ? null : form.getName()) + '}';
    }
}
