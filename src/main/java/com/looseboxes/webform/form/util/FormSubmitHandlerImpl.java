package com.looseboxes.webform.form.util;

import com.looseboxes.webform.web.FormConfig;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.looseboxes.webform.Errors;
import java.util.Objects;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.repository.EntityRepository;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.domain.UpdateEntityAndNestedIfAny;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.web.FormRequest;

/**
 * @author hp
 */
public class FormSubmitHandlerImpl implements FormSubmitHandler{
    
//    private static final Logger LOG = LoggerFactory.getLogger(FormSubmitHandlerImpl.class);
    
    private final UpdateEntityAndNestedIfAny saveEntityAndChildrenIfAny;
    private final TypeFromNameResolver entityTypeResolver;
    private final EntityRepositoryProvider entityRepositoryProvider;
    
    public FormSubmitHandlerImpl(
            UpdateEntityAndNestedIfAny saveEntityAndChildrenIfAny,
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
        
        FormConfigDTO formConfig = formRequest.getFormConfig();
                    
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
                // We delete images in the root entity only
                // This is because We we delete a product and the product
                // has a nested user, then we need not delete the product's user
                //
                this.saveEntityAndChildrenIfAny.deleteRootOnly(formRequest);
                break;
                
            default:
                throw Errors.unexpectedElement(crudAction, CRUDAction.values());
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
