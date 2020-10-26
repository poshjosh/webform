package com.looseboxes.webform.domain;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.TypeTests;
import com.bc.webform.form.FormBean;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.services.ModelObjectService;
import com.looseboxes.webform.web.FormRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class UpdateEntityAndNestedIfAny {
    
    private static final Logger LOG = LoggerFactory.getLogger(UpdateEntityAndNestedIfAny.class);
    
    private final TypeFromNameResolver typeFromNameResolver;
    private final EntityRepositoryProvider entityRepositoryProvider;
    private final TypeTests typeTests;
    private final ObjectGraphBuilder objectGraphBuilder;  
    private final ModelObjectService modelObjectService;
    private final ModelUpdater modelUpdater;
    
    public UpdateEntityAndNestedIfAny(
            TypeFromNameResolver entityTypeResolver, 
            EntityRepositoryProvider entityRepositoryProvider,
            TypeTests typeTests,
            ObjectGraphBuilder objectGraphListBuilder,
            ModelObjectService modelObjectService,
            ModelUpdater modelUpdater) {
        this.typeFromNameResolver = Objects.requireNonNull(entityTypeResolver);
        this.entityRepositoryProvider = Objects.requireNonNull(entityRepositoryProvider);
        this.typeTests = Objects.requireNonNull(typeTests);
        this.objectGraphBuilder = Objects.requireNonNull(objectGraphListBuilder);
        this.modelObjectService = Objects.requireNonNull(modelObjectService);
        this.modelUpdater = Objects.requireNonNull(modelUpdater);
//        LOG.trace("TypeFromNameResolver: {}, EntityRepositoryProvider: {}", 
//                typeFromNameResolver, entityRepositoryProvider);
    }
    
    public Object save(FormRequest formRequest) {
        
        Object modelobject = formRequest.getFormConfig().getModelobject();
        
        return this.save(modelobject, formRequest);
    }
    
    public Object save(Object modelobject, FormRequest formRequest) {
        
        final List entityList = this.buildEntityList(modelobject, formRequest);
        
        final Object entity = entityList.get(entityList.size() - 1);
        
        Object result = null;
        
        for(Object child : entityList) {
            
            final Class childType = child.getClass();
            
            child = this.entityRepositoryProvider.forEntity(childType).save(child);
            
            LOG.debug("Inserted: {}", child);
            
            result = child;
        }
        
        return this.updateFormDataSource(formRequest, entity, result);
    }
    
    public Object deleteRootOnly(FormRequest formRequest) {
        
        Object modelobject = formRequest.getFormConfig().getModelobject();
        
        final List entityList = this.buildEntityList(modelobject, formRequest);
        
        final Object entity = entityList.get(entityList.size() - 1);
        
        Object id = this.entityRepositoryProvider.getIdOptional(entity).orElse(null);
        this.entityRepositoryProvider.forEntity(entity.getClass()).deleteById(id);

        LOG.debug("Deleted: {}", entity);
        
        return entity;
    }

    public Object delete(FormRequest formRequest) {
        
        Object modelobject = formRequest.getFormConfig().getModelobject();
        
        return this.delete(modelobject, formRequest);
    }
    
    public Object delete(Object modelobject, FormRequest formRequest) {
        
        final List entityList = this.buildEntityList(modelobject, formRequest);
        
        final Object entity = entityList.get(entityList.size() - 1);
        
        Object result = null;
        
        for(Object child : entityList) {
            
            final Class childType = child.getClass();
            
            Object id = this.entityRepositoryProvider.getIdOptional(child).orElse(null);
            if(id != null) {
                this.entityRepositoryProvider.forEntity(childType).deleteById(id);
                LOG.debug("Deleted: {}", child);
            }else{
                LOG.debug("Not deleted as has no ID: {}", child);
            }
            
            result = child;
        }
        
        return this.updateFormDataSource(formRequest, entity, result);
    }

    private Object updateFormDataSource(FormRequest formRequest, Object entity, Object result) {
        //@TODO
        // This is a temporary bug fix
        final Object dataSource = result == null ? entity : result;
        try{
            formRequest.getFormConfig().getFormOptional().ifPresent((form) -> {
                ((FormBean)form).setDataSource(dataSource);
            });
        }catch(RuntimeException e) {
            LOG.warn("Failed to set FormConfig.form.dataSource to: " + dataSource, e);
        }
        return result;
    }
    
    private List buildEntityList(Object model, FormRequest formRequest) {
        
        // The input model is converted to an entity if it is a DTO type
        // Hence the root may be the entity type of a DTO type if the
        // input model is a DTO type
        // 
        final List<Object> list = this.objectGraphBuilder.build(model);
        
        final Object lastEntity = list.get(list.size() - 1);
        
        final List result;
        if(list.size() == 1) {
            result = list;
        }else {

            // Entities that have no id, will always be equals so we use 
            // reference equality to distinguish them in this case
            //
            final Predicate isRoot = (e) -> e == lastEntity;
//            final Predicate hasNoId = (e) -> hasNoId(e);
            
            final boolean configure = modelObjectService.shouldConfigureModelObject(formRequest);
            
            // Configure all list elements but the root, which is already configured
            //
            final Function configureEntity = ! configure ? (e) -> e : 
                    (e) -> isRoot.test(e) ? e : configure(e, formRequest).orElse(null);
            
            result = (List)list.stream()
//                    .filter(isRoot.or(hasNoId))
                    // Returns null if there is no need to update the database
                    // for the particular entity
                    .map(configureEntity) 
                    .filter(e -> e != null)
                    .collect(Collectors.toList());
        }
        if(LOG.isTraceEnabled()) {
            LOG.trace("For update: {}", 
                    result.stream().map(Object::toString)
                            .collect(Collectors.joining("\n", "[\n", "\n]")));
        }
        return result;
    }
    
    private <S, T> Optional<T> configure(T modelobject, FormRequest<S> parentFormRequest) {
        
        modelobject = this.configureModelObject(modelobject, parentFormRequest);
        
        return this.fetchDatabaseInstanceAndUpdateWith(modelobject);
    }

    /**
     * Update the model with values from the database
     *
     * For each property, an update is only performed if the source has a
     * value and the target has a different or {@code null} value.
     *
     * If no update is performed, returns an empty optional
     * 
     * @param model The model to source values from
     * @return     An optional containing the updated model, or an empty optional
     * if no update was performed on the target.
     */
    private <T> Optional<T> fetchDatabaseInstanceAndUpdateWith(T model) 
            throws EntityNotFoundException{
        Object id = this.entityRepositoryProvider.getIdOptional(model).orElse(null);
        final T result;
        if(id == null) {
            result = model;
        }else{    
            final Class modelClass = model.getClass();
            final T targetEntity = (T)this.entityRepositoryProvider
                    .forEntity(modelClass)
                    .findById(id)
                    .orElse(null);
            if(targetEntity == null) {
                throw new EntityNotFoundException(modelClass.getSimpleName() + " with ID: " + id);
            }
            
            result = (T)modelUpdater.update(model, targetEntity).orElse(null);
        }
        return Optional.ofNullable(result);
    }
    
    private <S, T> T configureModelObject(T modelobject, FormRequest<S> parentFormRequest) {
    
        Class<T> modeltype = (Class<T>)modelobject.getClass();

        String modelname = this.typeFromNameResolver.getName(modeltype);

        FormRequest<T> formRequestUpdate = 
                modelObjectService.updateRequest(parentFormRequest, modelname, null);

        modelobject = modelObjectService.configureModelObject(modelobject, formRequestUpdate);
        
        return modelobject;
    }
    
    private boolean hasNoId(Object e) {
        return ! this.hasId(e);
    }
    
    private boolean hasId(Object e) {
        return entityRepositoryProvider.getIdOptional(e).isPresent();
    }

    public TypeFromNameResolver getTypeFromNameResolver() {
        return typeFromNameResolver;
    }

    public EntityRepositoryProvider getEntityRepositoryProvider() {
        return entityRepositoryProvider;
    }

    public TypeTests getTypeTests() {
        return typeTests;
    }

    public ObjectGraphBuilder getObjectGraphBuilder() {
        return objectGraphBuilder;
    }

    public ModelObjectService getModelObjectService() {
        return modelObjectService;
    }
}
