package com.looseboxes.webform.form;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.controllers.FormControllerHtml;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.looseboxes.webform.CrudEvent;

/**
 * @author hp
 */
public class OnFormSubmittedImpl implements 
        FormControllerHtml.OnFormSubmitted{
    
    private static final Logger LOG = LoggerFactory.getLogger(OnFormSubmittedImpl.class);
    
    private final TypeFromNameResolver entityTypeResolver;
    private final EntityRepositoryFactory entityRepositoryFactory;
            
    public OnFormSubmittedImpl(
            TypeFromNameResolver entityTypeResolver, 
            EntityRepositoryFactory entityRepositoryFactory) {
        this.entityTypeResolver = Objects.requireNonNull(entityTypeResolver);
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
    }
    
    @Override
    public void onFormSubmitted(FormConfig formReqParams) {
                    
        final Class entityType = this.getType(formReqParams);
        final EntityRepository repo = entityRepositoryFactory.forEntity(entityType);
        
        final CrudEvent crudAction = formReqParams.getCrudAction();
        switch(crudAction) {
            case create:
                final Object modelobject = formReqParams.getModelobject();
                repo.create(modelobject);
                if(LOG.isDebugEnabled()) {
                    final Object id = repo.getIdOptional(modelobject);
                    LOG.debug("Inserted object id: {}", id);
                }
                break;
            case read:
                break;
            case update:
                repo.update(findModelObject(formReqParams, repo));
                break;
            case delete:
                final Object id = formReqParams.getModelid();
                repo.deleteById(id);
                break;
            default:
                throw Errors.unexpected(crudAction, (Object[])CrudEvent.values());
        }   
    }
    
    public Class getType(FormConfig formReqParams) {
        final Object modelobject = formReqParams.getModelobject();
        final Class entityType = modelobject != null ? modelobject.getClass() : 
                entityTypeResolver.getType(formReqParams.getModelname());
        return entityType;
    }
    
    public Object findModelObject(FormConfig formReqParams, EntityRepository repo) {
        return formReqParams.getModelobject() != null ? 
                formReqParams.getModelobject() :
                repo.find(formReqParams.getModelid());
    }
}
