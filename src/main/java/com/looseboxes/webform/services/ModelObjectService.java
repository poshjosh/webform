package com.looseboxes.webform.services;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.looseboxes.webform.CrudEvent;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.exceptions.MalformedRouteException;
import com.looseboxes.webform.exceptions.TargetNotFoundException;
import com.looseboxes.webform.form.FormConfig;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hp
 */
@Service
public class ModelObjectService{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormService.class);

    public static final String FORM_ID_PREFIX = "form";
    
    private final EntityRepositoryFactory entityRepositoryFactory;
    private final TypeFromNameResolver typeFromNameResolver;

    @Autowired
    public ModelObjectService(
            EntityRepositoryFactory entityRepositoryFactory, 
            TypeFromNameResolver typeFromNameResolver) {
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
    }
    
    public Object getModel(FormConfig formConfig) {
        final CrudEvent crudAction = formConfig.getCrudAction();
        final String modelname = formConfig.getModelname();
        final String modelid = formConfig.getModelid();
        final Object object;
        switch(crudAction) {
            case create: 
                object = this.createModel(modelname);
                break;
            case read: 
            case update: 
            case delete: 
                object = this.getModel(modelname, modelid); 
                break;
            default: throw Errors.unexpected(crudAction, (Object[])CrudEvent.values());
        }
        return object;
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
}
