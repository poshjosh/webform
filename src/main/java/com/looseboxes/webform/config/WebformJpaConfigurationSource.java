package com.looseboxes.webform.config;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.jpa.spring.JpaConfiguration;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.TypeTests;
import com.bc.webform.TypeTestsImpl;
import com.looseboxes.webform.mappers.EntityMapperService;
import com.looseboxes.webform.form.util.FormSubmitHandler;
import com.looseboxes.webform.form.util.FormSubmitHandlerImpl;
import com.looseboxes.webform.form.validators.EntityUniqueColumnsValidator;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.form.validators.FormValidatorFactoryImpl;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.repository.EntityRepositoryProviderImpl;
import com.looseboxes.webform.repository.MappedEntityRepositoryProvider;
import com.looseboxes.webform.domain.MappedEntityTypeFromNameResolver;
import com.looseboxes.webform.services.ModelObjectService;
import com.looseboxes.webform.domain.UpdateEntityAndNestedIfAny;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import com.looseboxes.webform.domain.ObjectGraphBuilder;
import com.looseboxes.webform.domain.WebformObjectGraphBuilder;
import com.looseboxes.webform.domain.GetUniqueColumnNames;
import com.looseboxes.webform.domain.GetUniqueColumnNamesImpl;
import com.looseboxes.webform.domain.ModelUpdater;
import com.looseboxes.webform.domain.ModelUpdaterImpl;

/**
 * @author hp
 */
public class WebformJpaConfigurationSource extends JpaConfiguration{
    
    private final ApplicationContext applicationContext;

    public WebformJpaConfigurationSource(ApplicationContext applicationContext) {
        super(applicationContext);
        this.applicationContext = Objects.requireNonNull(applicationContext);
    }
    
    @Bean public GetUniqueColumnNames getUniqueColumnNames(
            EntityMapperService entityMapperService, MetaDataAccess metaDataAccess) {
        return new GetUniqueColumnNamesImpl(entityMapperService, metaDataAccess);
    }
    
    @Bean public TypeTests typeTests() {
        return new TypeTestsImpl().withDomainTest(this.domainClasses());
    }
    
    @Bean public ObjectGraphBuilder objectGraphBuilder(TypeTests typeTests) {
        return new WebformObjectGraphBuilder(typeTests, this.entityMapperService());
    }
    
    @Bean public UpdateEntityAndNestedIfAny saveEntityAndChildrenIfAny(
            TypeFromNameResolver typeFromNameResolver, EntityRepositoryProvider entityRepositoryProvider,
            TypeTests typeTests, ObjectGraphBuilder objectGraphBuilder,
            ModelObjectService modelObjectService, ModelUpdater modelUpdater) {
        return new UpdateEntityAndNestedIfAny(
                typeFromNameResolver, 
                entityRepositoryProvider,
                typeTests,
                objectGraphBuilder,
                modelObjectService,
                modelUpdater
        );
    }
    
    @Bean public ModelUpdater modelUpdater() {
        return new ModelUpdaterImpl();
    }
    
    @Bean public FormSubmitHandler formSubmitHandler(
            @Autowired UpdateEntityAndNestedIfAny saveEntityAndChildrenIfAny) {
        return new FormSubmitHandlerImpl(
                saveEntityAndChildrenIfAny,
                this.typeFromNameResolver(), 
                this.entityRepositoryProvider()
        );
    }
    
    @Bean public FormValidatorFactory formValidatorFactory(EntityUniqueColumnsValidator validator) {
        return new FormValidatorFactoryImpl(validator);
    }
    
    @Bean public EntityUniqueColumnsValidator entityUniqueColumnsValidator(
            GetUniqueColumnNames getUniqueColumnNames, EntityRepositoryProvider repositoryProvider) {
        return new EntityUniqueColumnsValidator(getUniqueColumnNames, repositoryProvider);
    }

    @Override 
    @Bean public TypeFromNameResolver typeFromNameResolver() {
        return new MappedEntityTypeFromNameResolver(
                this.entityMapperService(), super.typeFromNameResolver());
    }
        
    @Bean public EntityRepositoryProvider entityRepositoryProvider() {
        return new MappedEntityRepositoryProvider(
                this.entityMapperService(), this.unMappedEntityRepositoryProvider());
    }

    public EntityRepositoryProvider unMappedEntityRepositoryProvider() {
        return new EntityRepositoryProviderImpl(
                this.jpaObjectFactory(), this.domainClasses(), this.entityIdAccessor());
    }
    
    private EntityMapperService entityMapperService() {
        return applicationContext.getBean(EntityMapperService.class);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
