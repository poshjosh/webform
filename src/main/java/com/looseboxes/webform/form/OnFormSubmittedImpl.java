package com.looseboxes.webform.form;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.looseboxes.webform.CrudActionNames;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.FormController;
import com.looseboxes.webform.Print;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class OnFormSubmittedImpl implements 
        FormController.OnFormSubmitted, CrudActionNames{
    
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
    public void onFormSubmitted(FormRequestParams formReqParams) {
                    
        final Class entityType = entityTypeResolver.getType(formReqParams.getModelname());
        final EntityRepository repo = entityRepositoryFactory.forEntity(entityType);
        
        final String action = formReqParams.getAction();
        switch(action) {
            case CREATE:
                final Object modelobject = formReqParams.getModelobject();
                repo.create(modelobject);
//                this.printLastInserted(repo);
                break;
            case READ:
                break;
            case UPDATE:
                repo.update(getModelObject(repo, formReqParams));
                break;
            case DELETE:
                final Object id = formReqParams.getModelid();
                repo.deleteById(id);
                break;
            default:
                throw Errors.unexpectedAction(action);
        }   
    }
    
    public Object getModelObject(EntityRepository repo, FormRequestParams formReqParams) {
        return formReqParams.getModelobject() != null ? 
                formReqParams.getModelobject() :
                repo.find(formReqParams.getModelid());
    }
    
    private void printLastInserted(EntityRepository repo) {
        if(LOG.isTraceEnabled()) {
            if(repo.count() < 1000) {
                final List found = repo.findAll();
                if(found.isEmpty()) {
                    throw new IllegalStateException();
                }
                final Object last = found.get(found.size() - 1);
                new Print().trace("Printing inserted object:\n", last, "\n", "");
            }
        }
    }
}
