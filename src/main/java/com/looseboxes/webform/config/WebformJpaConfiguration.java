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
import com.looseboxes.webform.form.FormSubmitHandler;
import com.looseboxes.webform.form.validators.EntityUniqueColumnsValidator;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.services.ModelObjectService;
import com.looseboxes.webform.util.ObjectGraphAsListBuilder;
import com.looseboxes.webform.util.SaveEntityAndChildrenIfAny;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hp
 */
@Configuration
@ConditionalOnMissingBean(WebformJpaConfigurationSource.class)
public class WebformJpaConfiguration{
    
    private final WebformJpaConfigurationSource delegate;
    
    public WebformJpaConfiguration(ApplicationContext context) {
        delegate = new WebformJpaConfigurationSource(context);
    }

    @Bean public TypeTests typeTests() {
        return delegate.typeTests();
    }

    @Bean public ObjectGraphAsListBuilder objectGraphListBuilder() {
        return delegate.objectGraphListBuilder();
    }

    @Bean public SaveEntityAndChildrenIfAny saveEntityAndChildrenIfAny(
            ModelObjectService modelObjectService) {
        return delegate.saveEntityAndChildrenIfAny(modelObjectService);
    }

    @Bean public FormSubmitHandler formSubmitHandler(
            SaveEntityAndChildrenIfAny saveEntityAndChildrenIfAny) {
        return delegate.formSubmitHandler(saveEntityAndChildrenIfAny);
    }

    @Bean public FormValidatorFactory formValidatorFactory() {
        return delegate.formValidatorFactory();
    }

    @Bean public EntityUniqueColumnsValidator entityUniqueColumnsValidator() {
        return delegate.entityUniqueColumnsValidator();
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
