package com.looseboxes.webform.services;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.Form;
import com.bc.webform.FormMember;
import com.bc.webform.functions.FormInputContext;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.FormEndpoints;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.form.FormRequestParams;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.Wrapper;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.exceptions.InvalidRouteException;
import com.looseboxes.webform.exceptions.MalformedRouteException;
import com.looseboxes.webform.exceptions.TargetNotFoundException;
import com.looseboxes.webform.store.AttributeStore;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.exceptions.FormUpdateException;
import java.lang.reflect.Field;
import com.looseboxes.webform.CrudActionName;
import static com.looseboxes.webform.CrudActionName.create;
import static com.looseboxes.webform.CrudActionName.read;
import static com.looseboxes.webform.CrudActionName.update;
import static com.looseboxes.webform.CrudActionName.delete;

/**
 * @author hp
 */
@Service
public class FormService implements Wrapper<StoreDelegate, FormService>, FormFactory{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormService.class);

    public static final String FORM_ID_PREFIX = "form";
    
    private final EntityRepositoryFactory entityRepositoryFactory;
    private final TypeFromNameResolver typeFromNameResolver;
    private final AttributeService attributeService;
    private final FormFactory formFactory;
    private final FormInputContext<Object, Field, Object> formInputContext;
    private final FormEndpoints formEndpoints;

    @Autowired
    public FormService(
            EntityRepositoryFactory entityRepositoryFactory, 
            TypeFromNameResolver typeFromNameResolver,
            AttributeService attributeService,
            FormFactory formFactory,
            FormInputContext<Object, Field, Object> formInputContext,
            FormEndpoints formEndpoints) {
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
        this.attributeService = Objects.requireNonNull(attributeService);
        this.formFactory = Objects.requireNonNull(formFactory);
        this.formInputContext = Objects.requireNonNull(formInputContext);
        this.formEndpoints = Objects.requireNonNull(formEndpoints);
    }

    @Override
    public FormService wrap(StoreDelegate delegate) {
        return new FormService(
                this.entityRepositoryFactory,
                this.typeFromNameResolver,
                this.attributeService.wrap(delegate),
                this.formFactory,
                this.formInputContext,
                this.formEndpoints
        );
    }

    @Override
    public StoreDelegate unwrap() {
        return this.attributeService.unwrap();
    }
    
    public Object begin(String action, String modelname, String modelid) {
        final CrudActionName crudAction = CrudActionName.valueOf(action);
        final Object object;
        switch(crudAction) {
            case create: object = this.beginCreate(modelname); break;
            case read: object = this.beginRead(modelname, modelid); break;
            case update: object = this.beginUpdate(modelname, modelid); break;
            case delete: object = this.beginDelete(modelname, modelid); break;
            default: throw Errors.unexpected(crudAction, (Object[])CrudActionName.values());
        }
        return object;
    }
    
    public Object beginCreate(String modelname) {
        return this.createModel(modelname);
    }
    
    public Object beginRead(String modelname, String modelid) {
        return this.getModelAndClearAttributes(modelname, modelid);
    }
    
    public Object beginUpdate(String modelname, String modelid){
        return this.getModelAndClearAttributes(modelname, modelid);
    }
    
    public Object beginDelete(String modelname, String modelid){
        return this.getModelAndClearAttributes(modelname, modelid);
    }
    
    public Object getModelAndClearAttributes(String modelname, String modelid){ 
        return this.getModel(modelname, modelid);
    }
    
    public Object getModel(String modelname, String modelid) {
        if(modelid == null || modelid.isEmpty()) {
            throw new AttributeNotFoundException(modelname, Params.MODELID);
        }
        final String errMsg = modelname + " with id = " + modelid;
        Object found = null;
        try{
            found = this.fetchModelFromDatabase(modelname, modelid);
        }catch(javax.persistence.EntityNotFoundException e){
            LOG.debug(errMsg, e);
            throw new TargetNotFoundException(errMsg, e);
        }
        if(found == null) {
            throw new TargetNotFoundException(errMsg);
        }
        return found;
    }

    public Object fetchModelFromDatabase(String modelname, String modelid) {
    
        final String errMsg = modelname + " not found";
        final Class modeltype = this.typeFromNameResolver.getTypeOptional(
                modelname).orElseThrow(() -> new MalformedRouteException(errMsg));
        
        final EntityRepository entityService = this.entityRepositoryFactory.forEntity(modeltype);

        final Object modelobject = entityService.find(modelid);

        LOG.debug("{} {} = {};", modeltype.getName(), modelname, modelobject);

        return modelobject;
    }
    
    public Object createModel(String modelname) {
        
        final String errMsg = modelname + " not found";

        Object modelobject = this.typeFromNameResolver
                .newInstanceOptional(modelname)
                .orElseThrow(() -> new MalformedRouteException(errMsg));

        LOG.debug("Newly created modelobject: {}", modelobject);
        
        return modelobject;
    }

    public void checkAll(FormRequestParams params){

        Objects.requireNonNull(params.getAction());
        Objects.requireNonNull(params.getFormid());
        final String modelname = Objects.requireNonNull(params.getModelname());
        final Object modelobject = Objects.requireNonNull(params.getModelobject());
        
        final String foundname = this.typeFromNameResolver.getName(modelobject.getClass());
        
        if( ! modelname.equalsIgnoreCase(foundname)) {
            
            LOG.warn("Expected name: {}, found name: {} from type: {}", 
                    modelname, foundname, modelobject.getClass());
        
            throw Errors.unexpectedModelName(modelname, foundname);
        }
    }

    public FormRequestParams params(
            String action, 
            String modelname, @Nullable String modelid,
            Object modelobject, String [] modelfields,
            @Nullable String parentFormId, @Nullable String targetOnCompletion) {

        return this.toRequestParams(
                false, false, action, generateFormId(), 
                modelname, modelid, modelobject, modelfields, 
                parentFormId, targetOnCompletion);
    }

    public FormRequestParams paramsForValidate(
            String action, @Nullable String formid, 
            String modelname, @Nullable String modelid,
            Object modelobject, String [] modelfields,
            @Nullable String parentFormId, @Nullable String targetOnCompletion) {
        
        return this.toRequestParams(
                true, false, action, formid, modelname, modelid, 
                modelobject, modelfields, parentFormId, targetOnCompletion);
    }
    
    public FormRequestParams paramsForSubmit(
            String action, @Nullable String formid, 
            String modelname, @Nullable String modelid,
            String [] modelfields,
            @Nullable String parentFormId, @Nullable String targetOnCompletion) {
        
        return this.toRequestParams(
                true, true, action, formid, modelname, modelid, null, 
                modelfields, parentFormId, targetOnCompletion);
    }

    public FormRequestParams toRequestParams(
            boolean existingForm, boolean useExistingModelObject,
            String action, String formid, 
            String modelname, @Nullable String modelid,
            Object modelobject, String [] modelfields,
            @Nullable String parentFormId, @Nullable String targetOnCompletion) {
        
        if(! CrudActionName.create.equals(action) && modelid == null) {
            throw new AttributeNotFoundException(modelname, Params.MODELID);
        }
        
        FormRequestParams formReqParams = ! existingForm ?
                this.getFormConfigAttribute(formid, modelname, null) :
                this.getFormConfigAttributeOrException(formid, modelname);
        
        LOG.debug("Existing params: {}\nexisting   form: {}", formReqParams,
                (formReqParams==null?null:formReqParams.getFormOptional().orElse(null)));

        if(existingForm && formReqParams == null) {
            throw new InvalidRouteException();
        }

        final Form parentForm = parentFormId == null ? null : 
                this.getFormOrException(parentFormId, modelname);
        
        if (useExistingModelObject) {
            modelobject = formReqParams.getModelobject();
        }

        final Form form = this.newForm(parentForm, formid, modelname, modelobject);

        if(formReqParams == null) {
            
            formReqParams = new FormRequestParams.Builder()
                    .action(action)
                    .formid(formid)
                    .modelname(modelname)
                    .modelid(modelid)
                    .modelobject(modelobject)
                    .modelfields(modelfields)
                    .targetOnCompletion(targetOnCompletion)
                    .form(form)
                    .build();
        }else{
        
            this.validate(formReqParams, 
                    action, formid, modelname, modelid, modelobject, 
                    modelfields, parentFormId, targetOnCompletion);
            
            formReqParams = this.update(formReqParams, form, modelobject);
        }
        
        return formReqParams;
    }   
    
    public void validate(FormRequestParams params, 
            String action, @Nullable String formid, 
            String modelname, @Nullable String modelid,
            Object modelobject, String [] modelfields,
            @Nullable String parentFormId, @Nullable String targetOnCompletion) {
    
        this.validate(Params.ACTION, params.getAction(), action);
        this.validate(Params.FORMID, params.getFormid(), formid);
        this.validate(Params.MODELNAME, params.getModelname(), modelname);
        this.validate(Params.MODELID, params.getModelid(), modelid);
        // This is changed with each stage of the form
//        this.validate(params.getModelobject(), modelobject);
        //@TODO null and empty arrays/lists are equal
//        this.validate(Params.MODELFIELDS, params.getModelfields(), modelfields);
        this.validate(Params.PARENT_FORMID, params.getParentFormid(), parentFormId);
        this.validate(Params.TARGET_ON_COMPLETION, params.getTargetOnCompletion(), targetOnCompletion);
    }
    
    public void validate(String name, Object expected, Object found) {
        if( ! Objects.equals(expected, found)) {
            throw new InvalidRouteException(
                    "For: " + name + "\nExpected: " + 
                    expected + "\n   Found: " + found);
        }
    }
    
    public boolean updateParentWithNewlyCreated(FormRequestParams params) 
            throws FormUpdateException{
        
        if( ! CrudActionName.create.equals(params.getAction())) {
            throw new UnsupportedOperationException(
                    "Only 'create' supported but found: " + params.getAction());
        }

        final Form<Object> form = params.getFormOptional()
                .orElseThrow(() -> new NullPointerException());
        
        final Form<Object> parent = form.getParent();
        if(parent == null) {
            return false;
        }
        
        final String memberName = form.getName();
        final FormMember member = parent.getMember(memberName).orElse(null);
        if(member == null) {
            throw parentUpdateException(FormRequestParams.class, "name: "+memberName);
        }
        
        this.updateForm(form, memberName, params.getModelobject());
        
        return true;
    }
    
    public void updateForm(Form form, String name, Object value) 
            throws FormUpdateException{
        
        final String formid = form.getId();
        
        final String modelname = form.getName();
        
        final FormRequestParams formReqParams = getFormConfigAttribute(
                formid, modelname, null);
        
        if(formReqParams == null) {
            throw parentUpdateException(FormRequestParams.class, 
                    String.format("formid: %1s, modelname: %2s", formid, modelname));
        }
        
        final Object modelobject = formReqParams.getModelobject();
        
        Objects.requireNonNull(modelobject);

        try{
            final Field field = modelobject.getClass().getField(name);
            Objects.requireNonNull(field);
            formInputContext.setValue(modelobject, field, value);
        }catch(NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
        
        final Form updateForm = newForm(form.getParent(), formid, modelname, modelobject);
        
        final FormRequestParams update = update(formReqParams, updateForm, modelobject);
        
        this.setSessionAttribute(update);
    }

    public FormRequestParams update(
            FormRequestParams formReqParams, Form form, Object modelobject) {
        return new FormRequestParams.Builder()
                .with(formReqParams)
                .form(form)
                .modelobject(modelobject)
                .build();
    }

    public FormUpdateException parentUpdateException(Class target, Object params) {
        return this.parentUpdateException(target.getSimpleName(), params);
    }
    
    public FormUpdateException parentUpdateException(String target, Object params) {
        final String msg = target + " not found for: " + params;
        return new FormUpdateException(msg);
    }
    
    /**
     * Form ids need to be unique within a session.
     * @return 
     */
    public String generateFormId() {
        return FORM_ID_PREFIX + Long.toHexString(System.currentTimeMillis());
    }

    public Form getFormOrException(String formid, String modelname) {
        
        Objects.requireNonNull(formid);
        Objects.requireNonNull(modelname);
        
        final FormRequestParams parentFormParams = 
                getFormConfigAttributeOrException(formid, modelname);
        
        final Form parentForm = parentFormParams.getFormOptional()
                .orElseThrow(() -> new InvalidRouteException());
    
        LOG.trace("For id: {}, found form: {}", formid, parentFormParams);
        
        return parentForm;
    }  
    
    public FormRequestParams getFormConfigAttributeOrException(
            String formid, String modelname) {
        
        Objects.requireNonNull(formid);
        Objects.requireNonNull(modelname);
        
        final FormRequestParams formReqParams = getSessionAttribute(formid, null);
        
        if(formReqParams == null) {
            throw new InvalidRouteException();
        }
        
        return formReqParams;
    }
    
    public FormRequestParams getFormConfigAttribute(
            String formid, String modelname, FormRequestParams resultIfNone) {
        Objects.requireNonNull(formid);
        Objects.requireNonNull(modelname);
        final FormRequestParams formReqParams = formid == null ? 
                null : this.getSessionAttribute(formid, null);
        return formReqParams == null ? resultIfNone : formReqParams;
    }
    
    public FormRequestParams getSessionAttribute(
            String formid, FormRequestParams resultIfNone) {
        final String attributeName = this.getAttributeName(formid);
        Objects.requireNonNull(attributeName);
        final Object value = this.sessionAttributes()
                .getOrDefault(attributeName, null);
        LOG.trace("Get {} = {}", attributeName, value);
        return (FormRequestParams)value;
    }

    public void removeSessionAttribute(String formid) {
        Objects.requireNonNull(formid);
        final String name = getAttributeName(formid);
        final Object removed = sessionAttributes().remove(name);  
        LOG.trace("Removed {} = {}", name, removed);
    }
    
    public void setSessionAttribute(FormRequestParams formReqParams){
        final String attributeName = this.getAttributeName(formReqParams);
        Objects.requireNonNull(attributeName);
        this.sessionAttributes().put(attributeName, formReqParams);
        LOG.trace("Set {} = {}", attributeName, formReqParams);
    }

    public String getAttributeName(FormRequestParams formReqParams) {
        return this.getAttributeName(formReqParams.getFormid());
    }
    
    public String getAttributeName(String formid) {
        Objects.requireNonNull(formid);
        return HttpSessionAttributes.formReqParams(formid);
    }
    
    public AttributeStore<HttpSession> sessionAttributes() {
        return this.attributeService.sessionAttributes();
    }

    @Override
    public Form newForm(Form parentForm, String id, String name){
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        return formFactory.newForm(parentForm, id, name);
    }

    @Override
    public Form newForm(Form parentForm, String id, String name, Object object) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(object);
        return formFactory.newForm(parentForm, id, name, object);
    }

    public FormEndpoints getFormEndpoints() {
        return formEndpoints;
    }
}
