package com.looseboxes.webform.form;

import com.looseboxes.webform.web.FormConfig;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.functions.TypeTests;
import com.looseboxes.webform.Errors;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.configurers.EntityMapperService;
import com.looseboxes.webform.repository.EntityRepository;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.services.ModelObjectService;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import com.looseboxes.webform.util.ObjectGraphAsListBuilder;
import com.looseboxes.webform.web.FormRequest;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hp
 */
public class FormSubmitHandlerImpl implements FormSubmitHandler{
    
    private static final Logger LOG = LoggerFactory.getLogger(FormSubmitHandlerImpl.class);
    
    private final TypeFromNameResolver entityTypeResolver;
    private final EntityRepositoryProvider entityRepositoryProvider;
    private final TypeTests typeTests;
    private final EntityMapperService entityMapperService;
    private final ObjectGraphAsListBuilder objectGraphListBuilder;  
    private final ModelObjectService modelObjectService;
    
    public FormSubmitHandlerImpl(
            TypeFromNameResolver entityTypeResolver, 
            EntityRepositoryProvider entityRepositoryProvider,
            TypeTests typeTests,
            EntityMapperService entityMapperService,
            ObjectGraphAsListBuilder objectGraphListBuilder,
            ModelObjectService modelObjectService) {
        this.entityTypeResolver = Objects.requireNonNull(entityTypeResolver);
        this.entityRepositoryProvider = Objects.requireNonNull(entityRepositoryProvider);
        this.typeTests = Objects.requireNonNull(typeTests);
        this.entityMapperService = Objects.requireNonNull(entityMapperService);
        this.objectGraphListBuilder = Objects.requireNonNull(objectGraphListBuilder);
        this.modelObjectService = Objects.requireNonNull(modelObjectService);
//        LOG.trace("TypeFromNameResolver: {}, EntityRepositoryProvider: {}", 
//                entityTypeResolver, entityRepositoryProvider);
    }
    
    @Override
    public void process(FormRequest formRequest) {
        
        FormConfig formConfig = formRequest.getFormConfig();
                    
        final CRUDAction crudAction = formConfig.getCrudAction();
        switch(crudAction) {
            case create:
                this.saveEntityAndChildrenIfAny(formRequest);
                break;
                
            case read:
                break;
                
            case update:
                this.saveEntityAndChildrenIfAny(formRequest);
                break;
                
            case delete:
                final Object id = formConfig.getModelid();
                this.getEntityRepository(formConfig).deleteById(id);
                break;
                
            default:
                throw Errors.unexpected(crudAction, (Object[])CRUDAction.values());
        }   
    }
    
    public void saveEntityAndChildrenIfAny(FormRequest formRequest) {
        
        final Object entity = this.getEntity(formRequest);
        
        final List entityList = this.buildEntityList(entity, formRequest);
        
        for(Object child : entityList) {
            
            final Class childType = child.getClass();
            
            child = this.entityRepositoryProvider.forEntity(childType).save(child);
            
            LOG.debug("Inserted: {}", child);
        }
    }
    
    public Object getEntity(FormRequest formRequest) {
        
        Object modelobject = formRequest.getFormConfig().getModelobject();
        
        return this.entityMapperService.toEntity(modelobject);
    }
    
    public List buildEntityList(Object root, FormRequest formRequest) {
        
        // Accepting Enum types lead to Stackoverflow
        //
        
        Predicate<Field> test = (field) -> 
                typeTests.isDomainType(field.getType()) && ! typeTests.isEnumType(field.getType());
        
        final List<Object> list = this.objectGraphListBuilder.build(root, test);
        
        if(list.size() == 1) {
            return list;
        }else {
            
            // Configure all list elements but the root, which is already configured
            //
            // Entities that have no id, will always be equals so we use 
            // reference equality to distinguish them in this case
            //
            Function mapper = (e) -> e == root ? e : this.configureModelObject(e, formRequest);
            
            return (List)list.stream().map(mapper).collect(Collectors.toList());
        }
    }
    
    private <S, T> T configureModelObject(T modelobject, FormRequest<S> parentFormRequest) {
    
        Class<T> modeltype = (Class<T>)modelobject.getClass();
        String modelname = this.entityTypeResolver.getName(modeltype);
        String modelid = this.entityRepositoryProvider.forEntity(modeltype)
                .getIdOptional(modelobject)
                .map((id) -> id == null ? null : id.toString()).orElse(null);

        FormRequest<T> formRequestUpdate = 
                modelObjectService.updateRequest(parentFormRequest, modelname, modelid);
        
        modelobject = modelObjectService.configureModelObject(modelobject, formRequestUpdate);
        
        return modelobject;
    }
    
    public EntityRepository getEntityRepository(FormConfig formConfig) {
        return this.entityRepositoryProvider.forEntity(this.getType(formConfig));
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
                repo.findByIdOrException(formReqParams.getModelid());
    }
}
