package com.looseboxes.webform.form;

import com.looseboxes.webform.web.FormConfig;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.looseboxes.webform.Errors;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.entity.EntityRepository;
import com.looseboxes.webform.entity.EntityRepositoryProvider;

/**
 * @author hp
 */
public class FormSubmitHandlerImpl implements FormSubmitHandler{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormSubmitHandlerImpl.class);
    
    private final TypeFromNameResolver entityTypeResolver;
    private final EntityRepositoryProvider entityRepositoryFactory;
            
    public FormSubmitHandlerImpl(
            TypeFromNameResolver entityTypeResolver, 
            EntityRepositoryProvider entityRepositoryFactory) {
        this.entityTypeResolver = Objects.requireNonNull(entityTypeResolver);
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
    }
    
    @Override
    public void process(FormConfig formConfig) {
                    
        final Class entityType = this.getType(formConfig);
        final EntityRepository repo = entityRepositoryFactory.forEntity(entityType);
        
        final CRUDAction crudAction = formConfig.getCrudAction();
        switch(crudAction) {
            case create:
                final Object modelobject = formConfig.getModelobject();
                repo.create(modelobject);
                if(LOG.isDebugEnabled()) {
                    final Object id = repo.getIdOptional(modelobject);
                    LOG.debug("Inserted object id: {}", id);
                }
                break;
            case read:
                break;
            case update:
                repo.update(findModelObject(formConfig, repo));
                break;
            case delete:
                final Object id = formConfig.getModelid();
                repo.deleteById(id);
                break;
            default:
                throw Errors.unexpected(crudAction, (Object[])CRUDAction.values());
        }   
    }
    
    public Class getType(FormConfig formConfig) {
        final Object modelobject = formConfig.getModelobject();
        final Class entityType = modelobject != null ? modelobject.getClass() : 
                entityTypeResolver.getType(formConfig.getModelname());
        return entityType;
    }
    
    public Object findModelObject(FormConfig formReqParams, EntityRepository repo) {
        return formReqParams.getModelobject() != null ? 
                formReqParams.getModelobject() :
                repo.find(formReqParams.getModelid());
    }
}
