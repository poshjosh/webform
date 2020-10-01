package com.looseboxes.webform.web;

import com.bc.webform.form.Form;
import com.bc.webform.form.FormBean;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.Params;
import static com.looseboxes.webform.Params.ACTION;
import static com.looseboxes.webform.Params.FORMID;
import static com.looseboxes.webform.Params.MODELFIELDS;
import static com.looseboxes.webform.Params.MODELID;
import static com.looseboxes.webform.Params.MODELNAME;
import static com.looseboxes.webform.Params.PARENT_FORMID;
import static com.looseboxes.webform.Params.TARGET_ON_COMPLETION;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

/**
 * @author hp
 */
public class FormConfigBean  implements Serializable, FormConfig, Params {

    @NotNull
    private String action;
    
    @NotNull
    private String modelname;
    
    private String parentfid;

    private String fid;

    private String id;

    private List<String> modelfields;
    
    private String targetOnCompletion;
    
    private FormBean<Object> form;
    
    private Collection<String> uploadedFiles;
    
    private boolean buildAttempted;
    
    public FormConfigBean() { }
    
    public FormConfigBean(FormConfigBean form) { 
        this.init(form);
    }
    
    private void init(FormConfigBean arg) {
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
        this.uploadedFiles(arg.getUploadedFiles());
    }

    public void merge(FormConfigBean arg) {
        if(getCrudAction() == null) this.action(arg.getCrudAction());
        if(getForm() == null) this.form(arg.getForm());
        if(getFormid() == null) this.formid(arg.getFormid());
        if(getModelfields() == null || getModelfields().isEmpty()) {
            this.modelfields(arg.getModelfields() == null ? null :
                arg.getModelfields().isEmpty() ? Collections.EMPTY_LIST :
                Collections.unmodifiableList(arg.getModelfields()));
        }    
        if(getModelid() == null) this.modelid(arg.getModelid());
        if(getModelname() == null) this.modelname(arg.getModelname());
        if(getParentFormid() == null) this.parentFormid(arg.getParentFormid());
        if(getTargetOnCompletion() == null) this.targetOnCompletion(arg.getTargetOnCompletion());
        if(getUploadedFiles() == null) this.uploadedFiles(arg.getUploadedFiles());
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
    public FormConfigBean copy() {
        return new FormConfigBean(this);
    }
    
    public FormConfigBean with(FormConfigBean arg) {
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
        return this.id(arg);
    }

    public FormConfigBean id(String arg) {
        this.id = arg;
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
    
    public FormConfigBean form(FormBean form) {
        this.form = form;
        return this;
    }
    
    
    public FormConfigBean uploadedFiles(Collection<String> files) {
        this.uploadedFiles = files;
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
            case UPLOADED_FILES: 
                if(value instanceof Collection) {
                    return this.uploadedFiles((Collection<String>)value);
                }else if(value instanceof String[]){
                    return this.uploadedFiles(Arrays.asList((String[])value));
                }else{
                    if(value == null){
                        return this.uploadedFiles(null);
                    }else{
                        return this.uploadedFiles(Collections.singletonList(value.toString()));
                    }
                }
            case FormConfig.FORM: 
                return this.form(form == null || overwrite ? (FormBean)value : form);
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
                return this.id((id == null || overwrite) ? value : id);
            case TARGET_ON_COMPLETION: 
                return this.targetOnCompletion(
                        (targetOnCompletion == null || overwrite) ? value : targetOnCompletion);
            default: throw new IllegalArgumentException(
                    "Parameter named: " + name + ", not found for: " + this);
        }
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
        map.put(Params.TARGET_ON_COMPLETION, getTargetOnCompletion());
        map.put(Params.UPLOADED_FILES, getUploadedFiles());
        map.put(FormConfig.FORM, getForm());
        final Map<String, Object> result = Collections.unmodifiableMap(map);
        return result;
    }
    
    public void validate(FormConfig target) {
        //@related(FormConfig.fields) to front-end javascript/react
        this.validate(Params.ACTION, this.getCrudAction(), target.getCrudAction(), target);
        this.validate(Params.FORMID, this.getFormid(), target.getFormid(), target);
        this.validate(Params.MODELNAME, this.getModelname(), target.getModelname(), target);
        this.validate(Params.MODELID, this.getModelid(), target.getModelid(), target);
        this.validate(Params.MODELFIELDS, this.getModelfields(), target.getModelfields(), target);
        this.validate(Params.PARENT_FORMID, this.getParentFormid(), target.getParentFormid(), target);
        this.validate(Params.TARGET_ON_COMPLETION, this.getTargetOnCompletion(), target.getTargetOnCompletion(), target);
        this.validate(Params.UPLOADED_FILES, this.getUploadedFiles(), target.getUploadedFiles(), target);
    }
    
    private void validate(String name, Object expected, Object found, FormConfig target) {
        if( ! Objects.equals(expected, found)) {
            throw new ValidationException(
                    "For: " + name + "\nExpected: " + expected + "\n   Found: " + found + 
                            "\nExpected: " + this + "\n   Found: " + target);
        }
    }
    
    @Override
    public String getAction() {
        return action;
    }
    
    public void setAction(String crudAction) {
        this.action = crudAction;
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
    public String getId() {
        return id;
    }
    
    public void setId(String modelId) {
        this.id = modelId;
    }
    
    @Override
    public String getModelid() {
        return this.getId();
    }

    public void setModelid(String modelid) {
        this.setId(modelid);
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

    public void setForm(FormBean<Object> form) {
        this.form = form;
    }
    
    public Optional<Form<Object>> getParentFormOptional() {
        return Optional.ofNullable(form == null ? null : form.getParent());
    }
    
    public Optional<Form<Object>> getFormOptional() {
        return Optional.ofNullable(this.getForm());
    }

    @Override
    public FormBean<Object> getForm() {
        return this.form;
    }

    public Collection<String> removeUploadedFiles() {
        final Collection<String> result = this.getUploadedFiles();
        this.setUploadedFiles(null);
        return result;
    }
    
    public void setUploadedFiles(Collection<String> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    @Override
    public Collection<String> getUploadedFiles() {
        return uploadedFiles;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.action);
        hash = 41 * hash + Objects.hashCode(this.modelname);
        hash = 41 * hash + Objects.hash(this.parentfid);
        hash = 41 * hash + Objects.hash(this.fid);
        hash = 41 * hash + Objects.hashCode(this.id);
        hash = 41 * hash + Objects.hashCode(this.modelfields);
        hash = 41 * hash + Objects.hash(this.targetOnCompletion);
        hash = 41 * hash + Objects.hash(this.uploadedFiles);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.modelfields, other.modelfields)) {
            return false;
        }
        if (!Objects.equals(this.targetOnCompletion, other.targetOnCompletion)) {
            return false;
        }
        if (!Objects.equals(this.uploadedFiles, other.uploadedFiles)) {
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
                ", parent form id=" + parentfid + ", form id=" + fid + 
                ", model name=" + modelname + ", model id=" + id + 
                ", model fields=" + modelfields + ", target on completion=" +
                targetOnCompletion + ", uploaded files=" + uploadedFiles + 
                ", form: " + form + '}';
    }
}

