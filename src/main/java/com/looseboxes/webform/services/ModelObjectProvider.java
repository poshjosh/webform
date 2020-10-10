package com.looseboxes.webform.services;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.looseboxes.webform.repository.EntityRepository;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.exceptions.AttributeNotFoundException;
import com.looseboxes.webform.exceptions.MalformedRouteException;
import com.looseboxes.webform.exceptions.ResourceNotFoundException;
import com.looseboxes.webform.web.FormConfig;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import org.springframework.stereotype.Component;

/**
 * @author hp
 */
@Component
public class ModelObjectProvider{
    
    private final Logger LOG = LoggerFactory.getLogger(ModelObjectProvider.class);
    
    private final EntityRepositoryProvider entityRepositoryFactory;
    private final TypeFromNameResolver typeFromNameResolver;

    @Autowired
    public ModelObjectProvider(
            EntityRepositoryProvider entityRepositoryFactory, 
            TypeFromNameResolver typeFromNameResolver) {
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
    }
    
    public Object getModel(FormConfig formConfig) {
        final CRUDAction crudAction = formConfig.getCrudAction();
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
            default: throw Errors.unexpectedElement(crudAction, CRUDAction.values());
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
            throw new ResourceNotFoundException(errMsg, e);
        }
        if(found == null) {
            throw new ResourceNotFoundException(errMsg);
        }
        return found;
    }

    public Object fetchModelFromDatabase(String modelname, String modelid) {
    
        final String errMsg = modelname + " not found";
        final Class modeltype = this.typeFromNameResolver.getTypeOptional(
                modelname).orElseThrow(() -> new MalformedRouteException(errMsg));
        
        final EntityRepository jpaRepo = entityRepositoryFactory.forEntity(modeltype);

        final Object modelobject = jpaRepo.findByIdOrException(modelid);

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
