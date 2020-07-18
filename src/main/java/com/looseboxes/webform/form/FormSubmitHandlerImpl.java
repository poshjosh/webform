package com.looseboxes.webform.form;

import com.looseboxes.webform.web.FormConfig;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.looseboxes.webform.Errors;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.repository.EntityRepository;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.util.SaveEntityAndChildrenIfAny;
import com.looseboxes.webform.web.FormRequest;

/**
 * @author hp
 */
public class FormSubmitHandlerImpl implements FormSubmitHandler{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormSubmitHandlerImpl.class);
    
    private final SaveEntityAndChildrenIfAny saveEntityAndChildrenIfAny;
    private final TypeFromNameResolver entityTypeResolver;
    private final EntityRepositoryProvider entityRepositoryProvider;
    
    public FormSubmitHandlerImpl(
            SaveEntityAndChildrenIfAny saveEntityAndChildrenIfAny,
            TypeFromNameResolver entityTypeResolver, 
            EntityRepositoryProvider entityRepositoryProvider) {
        this.saveEntityAndChildrenIfAny = Objects.requireNonNull(saveEntityAndChildrenIfAny);
        this.entityTypeResolver = Objects.requireNonNull(entityTypeResolver);
        this.entityRepositoryProvider = Objects.requireNonNull(entityRepositoryProvider);
//        LOG.trace("TypeFromNameResolver: {}, EntityRepositoryProvider: {}", 
//                entityTypeResolver, entityRepositoryProvider);
    }
    
    @Override
    public void process(FormRequest formRequest) {
        
        FormConfig formConfig = formRequest.getFormConfig();
                    
        final CRUDAction crudAction = formConfig.getCrudAction();
        switch(crudAction) {
            case create:
                saveEntityAndChildrenIfAny.save(formRequest);
                break;
                
            case read:
                break;
                
            case update:
                saveEntityAndChildrenIfAny.save(formRequest);
                break;
                
            case delete:
                final Object id = formConfig.getModelid();
                this.getEntityRepository(formConfig).deleteById(id);
                break;
                
            default:
                throw Errors.unexpected(crudAction, (Object[])CRUDAction.values());
        }   
    }
    
    public EntityRepository getEntityRepository(FormConfig formConfig) {
        return this.entityRepositoryProvider.forEntity(this.getType(formConfig));
    }
    
    private Class getType(FormConfig formConfig) {
        final Object modelobject = formConfig.getModelobject();
        final Class entityType = modelobject != null ? modelobject.getClass() : 
                entityTypeResolver.getType(formConfig.getModelname());
        return entityType;
    }
}
