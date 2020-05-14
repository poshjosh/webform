package com.looseboxes.webform.services;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.Form;
import static com.looseboxes.webform.CrudActionNames.CREATE;
import static com.looseboxes.webform.CrudActionNames.DELETE;
import static com.looseboxes.webform.CrudActionNames.READ;
import static com.looseboxes.webform.CrudActionNames.UPDATE;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.form.FormRequestParams;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.Templates;
import com.looseboxes.webform.Wrapper;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.store.StoreDelegate;
import io.micrometer.core.lang.Nullable;
import java.io.FileNotFoundException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hp
 */
@Service
public class FormService implements Wrapper<StoreDelegate, FormService>, FormFactory{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormService.class);

    private final EntityRepositoryFactory entityRepositoryFactory;
    private final TypeFromNameResolver typeFromNameResolver;
    private final AttributeService attributeService;
    private final FormFactory formFactory;

    @Autowired
    public FormService(
            EntityRepositoryFactory entityRepositoryFactory, 
            TypeFromNameResolver typeFromNameResolver,
            AttributeService attributeService,
            FormFactory formFactory) {
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
        this.attributeService = Objects.requireNonNull(attributeService);
        this.formFactory = Objects.requireNonNull(formFactory);
    }

    @Override
    public FormService wrap(StoreDelegate delegate) {
        return new FormService(
                this.entityRepositoryFactory,
                this.typeFromNameResolver,
                this.attributeService.wrap(delegate),
                this.formFactory
        );
    }

    @Override
    public StoreDelegate unwrap() {
        return this.attributeService.unwrap();
    }
    
    public Object begin(String action, String modelname, String modelid) 
            throws FileNotFoundException{
        final Object object;
        switch(action) {
            case CREATE: object = this.beginCreate(modelname); break;
            case READ: object = this.beginRead(modelname, modelid); break;
            case UPDATE: object = this.beginUpdate(modelname, modelid); break;
            case DELETE: object = this.beginDelete(modelname, modelid); break;
            default: throw Errors.unexpectedAction(action);
        }
        return object;
    }
    
    public Object beginCreate(String modelname) {
        this.attributeService.removeAll(FormRequestParams.names());
        return this.createModel(modelname);
    }
    
    public Object beginRead(String modelname, String modelid) 
            throws FileNotFoundException{
        return this.getModelAndClearAttributes(modelname, modelid);
    }
    
    public Object beginUpdate(String modelname, String modelid) 
            throws FileNotFoundException{
        return this.getModelAndClearAttributes(modelname, modelid);
    }
    
    public Object beginDelete(String modelname, String modelid) 
            throws FileNotFoundException{
        return this.getModelAndClearAttributes(modelname, modelid);
    }
    
    public Object getModelAndClearAttributes(String modelname, String modelid) 
            throws FileNotFoundException{ 
        try{
            return this.getModel(modelname, modelid);
        }finally{
            this.attributeService.removeAll(FormRequestParams.names());
        }
    }
    
    public Object getModel(String modelname, String modelid) 
            throws FileNotFoundException{
        if(modelid == null || modelid.isEmpty()) {
            throw new AttributeNotFoundException(modelname, Params.MODELID);
        }
        final String errMsg = modelname + " with id = " + modelid;
        Object found = null;
        try{
            found = this.fetchModelFromDatabase(modelname, modelid);
        }catch(javax.persistence.EntityNotFoundException e){
            LOG.debug(errMsg, e);
            throw new FileNotFoundException(errMsg);
        }
        if(found == null) {
            throw new FileNotFoundException(errMsg);
        }
        return found;
    }

    public Object fetchModelFromDatabase(String modelname, String modelid) 
            throws javax.persistence.EntityNotFoundException{
    
        final Class modeltype = this.typeFromNameResolver.getType(modelname);
        
        final EntityRepository entityService = this.entityRepositoryFactory.forEntity(modeltype);

        final Object modelobject = entityService.find(modelid);

        LOG.debug("{} {} = {};", modeltype.getName(), modelname, modelobject);

        return modelobject;
    }
    
    public Object createModel(String modelname) {
        
        Object modelobject = this.typeFromNameResolver.newInstance(modelname);

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

    public FormRequestParams toRequestParams(
            String action, String modelname, 
            @Nullable String modelid,
            Object modelobject, String [] modelfields) {
        
        return this.toRequestParams(action, this.generateFormId(), 
                modelname, modelid, modelobject, modelfields);
    }
    
    /**
     * Form ids need to be unique within a session.
     * @return 
     */
    public String generateFormId() {
        return Long.toHexString(System.currentTimeMillis());
    }
    
    public FormRequestParams toRequestParams(
            String action, @Nullable String formid, 
            String modelname, @Nullable String modelid,
            Object modelobject, String [] modelfields) {
        
        final Form form = formFactory.newForm(modelname, modelobject);

        final FormRequestParams formReqParams = new FormRequestParams.Builder()
                .action(action)
                .formid(formid)
                .modelname(modelname)
                .modelid(modelid)
                .modelobject(modelobject)
                .modelfields(modelfields)
                .form(form)
                .build();
        
        return formReqParams;
    }    
    
    public String getTemplateForShowingForm(String action) {
        final String template;
        switch(action) {
            case CREATE:
                template = Templates.FORM; break;
            case READ:
                template = Templates.FORM_DATA; break;
            case UPDATE:
                template = Templates.FORM; break;
            case DELETE:
                template = Templates.FORM_CONFIRMATION; break;
            default:
                throw Errors.unexpectedAction(action);
        }
        return template;
    }

    @Override
    public Form newForm(String name) {
        return formFactory.newForm(name);
    }

    @Override
    public Form newForm(String name, Object object) {
        return formFactory.newForm(name, object);
    }
}
