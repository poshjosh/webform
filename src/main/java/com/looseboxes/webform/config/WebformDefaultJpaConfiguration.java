package com.looseboxes.webform.config;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.db.meta.access.functions.GetConnectionFromEntityManager;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.sql.SQLDateTimePatterns;
import com.bc.jpa.spring.DomainClasses;
import com.bc.jpa.spring.DomainClassesBuilder;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.jpa.spring.repository.JpaRepositoryFactory;
import com.bc.webform.TypeTests;
import com.looseboxes.webform.form.util.FormSubmitHandler;
import com.looseboxes.webform.form.validators.EntityUniqueColumnsValidator;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.services.ModelObjectService;
import com.looseboxes.webform.domain.UpdateEntityAndNestedIfAny;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.looseboxes.webform.domain.ObjectGraphBuilder;
import com.looseboxes.webform.mappers.EntityMapperService;
import com.looseboxes.webform.domain.GetUniqueColumnNames;
import com.looseboxes.webform.domain.ModelUpdater;

/**
 * @author hp
 */
@Configuration
@ConditionalOnMissingBean(WebformJpaConfigurationSource.class)
public class WebformDefaultJpaConfiguration{
    
    private final WebformJpaConfigurationSource delegate;
    
    public WebformDefaultJpaConfiguration(ApplicationContext context) {
        delegate = new WebformJpaConfigurationSource(context);
    }

    @Bean public GetUniqueColumnNames getUniqueColumnNames(EntityMapperService entityMapperService, MetaDataAccess metaDataAccess) {
        return delegate.getUniqueColumnNames(entityMapperService, metaDataAccess);
    }

    @Bean public TypeTests typeTests() {
        return delegate.typeTests();
    }

    @Bean public ObjectGraphBuilder objectGraphBuilder(TypeTests typeTests) {
        return delegate.objectGraphBuilder(typeTests);
    }

    @Bean public UpdateEntityAndNestedIfAny saveEntityAndChildrenIfAny(
            TypeFromNameResolver typeFromNameResolver, EntityRepositoryProvider entityRepositoryProvider,
            TypeTests typeTests, ObjectGraphBuilder objectGraphBuilder,
            ModelObjectService modelObjectService, ModelUpdater modelUpdater) {
        return delegate.saveEntityAndChildrenIfAny(
                typeFromNameResolver, entityRepositoryProvider, 
                typeTests, objectGraphBuilder, 
                modelObjectService, modelUpdater);
    }

    @Bean public ModelUpdater modelUpdater() {
        return delegate.modelUpdater();
    }

    @Bean public FormSubmitHandler formSubmitHandler(UpdateEntityAndNestedIfAny updateEntityAndChildrenIfAny) {
        return delegate.formSubmitHandler(updateEntityAndChildrenIfAny);
    }

    @Bean public FormValidatorFactory formValidatorFactory(EntityUniqueColumnsValidator validator) {
        return delegate.formValidatorFactory(validator);
    }

    @Bean public EntityUniqueColumnsValidator entityUniqueColumnsValidator(GetUniqueColumnNames getUniqueColumnNames, EntityRepositoryProvider repositoryProvider) {
        return delegate.entityUniqueColumnsValidator(getUniqueColumnNames, repositoryProvider);
    }

    @Bean public TypeFromNameResolver typeFromNameResolver() {
        return delegate.typeFromNameResolver();
    }

    @Bean public EntityRepositoryProvider entityRepositoryProvider() {
        return delegate.entityRepositoryProvider();
    }

    @Bean public DomainClasses domainClasses() {
        return delegate.domainClasses();
    }

    @Bean public DomainClassesBuilder domainClassesBuilder() {
        return delegate.domainClassesBuilder();
    }

    @Bean public JpaRepositoryFactory jpaRepositoryFactory() {
        return delegate.jpaRepositoryFactory();
    }

    @Bean public EntityRepositoryFactory entityRepositoryFactory() {
        return delegate.entityRepositoryFactory();
    }

    @Bean public MetaDataAccess metaDataAccess() {
        return delegate.metaDataAccess();
    }

    @Bean public GetConnectionFromEntityManager getConnectionFromEntityManager() {
        return delegate.getConnectionFromEntityManager();
    }

    @Bean public JpaObjectFactory jpaObjectFactory() {
        return delegate.jpaObjectFactory();
    }

    @Bean public SQLDateTimePatterns sqlDateTimePatterns() {
        return delegate.sqlDateTimePatterns();
    }
}
