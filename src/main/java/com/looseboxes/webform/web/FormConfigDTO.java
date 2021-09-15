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

package com.looseboxes.webform.web;

import com.bc.webform.form.Form;
import com.bc.webform.form.FormBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.FormStage;

import java.util.*;
import java.util.function.Function;

import org.springframework.validation.BindingResult;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:11:48 PM
 */
public class FormConfigDTO extends FormConfigBean{
    
//    private static final Logger LOG = LoggerFactory.getLogger(FormConfigDTO.class);
    
    // These fields are not copied when the copy method is used to create a 
    // new instance. This because they change between form stages
    // 
    @JsonIgnore
    private String stage;
    
    private Set<FormMessage> errors;
    
    private Set<FormMessage> infos;
    
    @JsonIgnore
    private BindingResult bindingResult;

    public FormConfigDTO() { }
    
    public FormConfigDTO(FormConfigBean form) { 
        super(form);
    }

    public void removeAllErrors(String fieldName) {
        this.removeAll(errors, fieldName);
    }
    
    public void removeAllInfos(String fieldName) {
        this.removeAll(infos, fieldName);
    }

    private void removeAll(Set<FormMessage> messages, String fieldName) {
        if(messages == null || messages.isEmpty()) {
            return;
        }
        final Iterator<FormMessage> iter = messages.iterator();
        while(iter.hasNext()) {
            final FormMessage message = iter.next();
            if(fieldName.equals(message.getFieldName())) {
                iter.remove();
            }
        }
    }

    @Override
    public FormConfigDTO copy() {
        return new FormConfigDTO(this);
    }
    
    @Override
    public FormConfigDTO with(FormConfigBean arg) {
        super.with(arg);
        return this;
    }
    
    @JsonIgnore
    public Optional<String> getRedirectForTargetOnCompletion() {
        final String targetOnCompletion = this.getTargetOnCompletion();
        return targetOnCompletion == null ? Optional.empty() : 
                Optional.of("redirect:" + targetOnCompletion);
    }    
    
    public boolean hasErrors() {
        return errors != null && ! errors.isEmpty();
    }
    
    public FormConfigDTO addErrors(Collection<FormMessage> errors) {
        errors.forEach((error) -> addError(error));
        return this;
    }
    
    public FormConfigDTO setErrors(Set<FormMessage> errors) {
        // We use our own copy to prevent external edits
        // Our copy is modifiable to allow modifications via the various addXXX methods
        this.errors = errors == null ? null : new LinkedHashSet(errors);
        return this;
    }

    public FormConfigDTO addErrorMessages(Collection<String> errors) {
        errors.forEach((error) -> this.addError(error));
        return this;
    }
    
    public FormConfigDTO addError(String error) {
        return this.addError(new FormMessage().objectName(this.getModelname()).message(error));
    }
    
    public FormConfigDTO addError(FormMessage error) {
        if(this.errors == null) {
            this.errors = new LinkedHashSet();
        }
        this.errors.add(error);
        return this;
    }

    public Collection<FormMessage> getErrors() {
        return this.errors;
    }
    
    public FormConfigDTO addInfos(Collection<FormMessage> infos) {
        infos.forEach((error) -> addInfo(error));
        return this;
    }

    public FormConfigDTO addInfoMessages(Collection<String> errors) {
        errors.forEach((info) -> this.addInfo(info));
        return this;
    }

    public FormConfigDTO addInfo(String info) { 
        return this.addInfo(new FormMessage().objectName(this.getModelname()).message(info));
    }
    
    public FormConfigDTO addInfo(FormMessage info) {
        if(this.infos == null) {
            this.infos = new LinkedHashSet();
        }
        this.infos.add(info);
        return this;
    }
    
    public FormConfigDTO setInfos(Set<FormMessage> infos) {
        // We use our own copy to prevent external edits
        // Our copy is modifiable to allow modifications via the various addXXX methods
        this.infos = infos == null ? null : new LinkedHashSet(infos);
        return this;
    }

    public Collection<FormMessage> getInfos() {
        return this.infos;
    }

    @JsonIgnore
    public BindingResult getBindingResult() {
        return bindingResult;
    }
    
    public FormConfigDTO bindingResult(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
        return this;
    }
    
    public void setBindingResult(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public FormConfigBean stage(FormStage stage) {
        return this.stage(stage.name());
    }
    public FormConfigBean stage(String stage) {
        this.stage = stage;
        return this;
    }
    @JsonIgnore
    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }
    
    @JsonIgnore
    public FormStage getFormStage() {
        return stage == null ? null : FormStage.valueOf(stage);
    }

    public void setFormStage(FormStage stage) {
        this.stage = stage.name();
    }
    
    @Override
    public FormConfigDTO form(FormBean form) {
        super.form(form); 
        return this;
    }

    @Override
    public FormConfigDTO targetOnCompletion(String target) {
        super.targetOnCompletion(target); 
        return this;
    }

    @Override
    public FormConfigDTO uploadedFiles(Collection<String> files) {
        super.uploadedFiles(files);
        return this;
    }

    @Override
    public FormConfigDTO modelfields(Collection<String> arg) {
        super.modelfields(arg);
        return this;
    }

    @Override
    public FormConfigDTO modelfields(String... args) {
        super.modelfields(args); 
        return this;
    }

    @Override
    public FormConfigDTO modelid(String arg) {
        super.modelid(arg); 
        return this;
    }

    @Override
    public FormConfigDTO fid(String arg) {
        super.fid(arg); 
        return this;
    }

    @Override
    public FormConfigDTO formid(String arg) {
        super.formid(arg); 
        return this;
    }

    @Override
    public FormConfigDTO parentfid(String arg) {
        super.parentfid(arg); 
        return this;
    }

    @Override
    public FormConfigDTO parentFormid(String arg) {
        super.parentFormid(arg); 
        return this;
    }

    @Override
    public FormConfigDTO modelname(String arg) {
        super.modelname(arg); 
        return this;
    }

    @Override
    public FormConfigDTO action(String arg) {
        super.action(arg); 
        return this;
    }

    @Override
    public FormConfigDTO action(CRUDAction arg) {
        super.action(arg); 
        return this;
    }

    @Override
    public FormConfigDTO id(String arg) {
        super.id(arg); 
        return this;
    }

    public String print() {
        return toString(form -> form.print());
    }

    @Override
    public String toString() {
        return toString(Objects::toString);
    }

    private String toString(Function<FormBean, String> formStringFunction) {
        return "->FormConfigDTO{\n->" + "action=" + getAction() +
                ", parent form id=" + getParentfid() + ", form id=" + getFid() +
                ", model name=" + getModelname() + ", model id=" + getId() +
                ", model fields=" + getModelfields() + ", target on completion=" +
                getTargetOnCompletion() + ", uploaded files=" + getUploadedFiles() +
                "\n->form: " + (getForm() == null ? null : formStringFunction.apply(getForm())) + "\n->stage: " + getStage() +
                ", errors: " + getErrors() + ", infos: " + getInfos() +
                '}';
    }
}
