package com.looseboxes.webform.util;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.TypeTests;
import com.bc.webform.form.FormBean;
import com.looseboxes.webform.mappers.EntityMapperService;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.services.ModelObjectService;
import com.looseboxes.webform.web.FormRequest;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class SaveEntityAndChildrenIfAny {
    
    private static final Logger LOG = LoggerFactory.getLogger(SaveEntityAndChildrenIfAny.class);
    
    private final TypeFromNameResolver typeFromNameResolver;
    private final EntityRepositoryProvider entityRepositoryProvider;
    private final TypeTests typeTests;
    private final EntityMapperService entityMapperService;
    private final ObjectGraphAsListBuilder objectGraphListBuilder;  
    private final ModelObjectService modelObjectService;
    
    public SaveEntityAndChildrenIfAny(
            TypeFromNameResolver entityTypeResolver, 
            EntityRepositoryProvider entityRepositoryProvider,
            TypeTests typeTests,
            EntityMapperService entityMapperService,
            ObjectGraphAsListBuilder objectGraphListBuilder,
            ModelObjectService modelObjectService) {
        this.typeFromNameResolver = Objects.requireNonNull(entityTypeResolver);
        this.entityRepositoryProvider = Objects.requireNonNull(entityRepositoryProvider);
        this.typeTests = Objects.requireNonNull(typeTests);
        this.entityMapperService = Objects.requireNonNull(entityMapperService);
        this.objectGraphListBuilder = Objects.requireNonNull(objectGraphListBuilder);
        this.modelObjectService = Objects.requireNonNull(modelObjectService);
//        LOG.trace("TypeFromNameResolver: {}, EntityRepositoryProvider: {}", 
//                typeFromNameResolver, entityRepositoryProvider);
    }
    
    public Object save(FormRequest formRequest) {
        
        Object modelobject = formRequest.getFormConfig().getModelobject();
        
        return this.save(modelobject, formRequest);
    }
    
    public Object save(Object modelobject, FormRequest formRequest) {
        
        final Object entity = this.entityMapperService.toEntity(modelobject);
        
        final List entityList = this.buildEntityList(entity, formRequest);
        
        Object result = null;
        
        for(Object child : entityList) {
            
            final Class childType = child.getClass();
            
            child = this.entityRepositoryProvider.forEntity(childType).save(child);
            
            LOG.debug("Inserted: {}", child);
            
            result = child;
        }

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
    
    private List buildEntityList(Object root, FormRequest formRequest) {
        
        // Accepting Enum types lead to Stackoverflow
        //
        final BiPredicate<Field, Object> test = (field, fieldValue) -> 
                typeTests.isDomainType(field.getType()) && 
                        ! typeTests.isEnumType(field.getType());
        
        final List<Object> list = this.objectGraphListBuilder.build(root, test);
        
        final List result;
        if(list.size() == 1) {
            result = list;
        }else {

            // Entities that have no id, will always be equals so we use 
            // reference equality to distinguish them in this case
            //
            final Predicate isRoot = (e) -> e == root;
            final Predicate hasNoId = (e) -> hasNoId(e);
            
            // Configure all list elements but the root, which is already configured
            //
            final Function configureNonRootEntity = (e) -> 
                    isRoot.test(e) ? e : configureModelObject(e, formRequest);
            
            result = (List)list.stream()
                    .filter(isRoot.or(hasNoId))
                    .map(configureNonRootEntity).collect(Collectors.toList());
        }
        if(LOG.isTraceEnabled()) {
            LOG.trace("For insert: {}", 
                    result.stream().map(Object::toString)
                            .collect(Collectors.joining("\n", "[\n", "\n]")));
        }
        return result;
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

    public EntityMapperService getEntityMapperService() {
        return entityMapperService;
    }

    public ObjectGraphAsListBuilder getObjectGraphListBuilder() {
        return objectGraphListBuilder;
    }

    public ModelObjectService getModelObjectService() {
        return modelObjectService;
    }
}
