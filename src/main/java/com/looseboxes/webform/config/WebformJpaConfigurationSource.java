package com.looseboxes.webform.config;

import com.bc.jpa.spring.JpaConfiguration;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.TypeTests;
import com.bc.webform.TypeTestsImpl;
import com.looseboxes.webform.mappers.EntityMapperService;
import com.looseboxes.webform.form.FormSubmitHandler;
import com.looseboxes.webform.form.FormSubmitHandlerImpl;
import com.looseboxes.webform.form.validators.EntityUniqueColumnsValidator;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.form.validators.FormValidatorFactoryImpl;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.repository.EntityRepositoryProviderImpl;
import com.looseboxes.webform.repository.MappedEntityRepositoryProvider;
import com.looseboxes.webform.repository.MappedEntityTypeFromNameResolver;
import com.looseboxes.webform.services.ModelObjectService;
import com.looseboxes.webform.util.ObjectAsGraphListBuilderImpl;
import com.looseboxes.webform.util.ObjectGraphAsListBuilder;
import com.looseboxes.webform.util.SaveEntityAndChildrenIfAny;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author hp
 */
public class WebformJpaConfigurationSource extends JpaConfiguration{
    
    private final ApplicationContext applicationContext;

    public WebformJpaConfigurationSource(ApplicationContext applicationContext) {
        super(applicationContext);
        this.applicationContext = Objects.requireNonNull(applicationContext);
    }
    
    @Bean public TypeTests typeTests() {
        return new TypeTestsImpl().withDomainTest(this.domainClasses());
    }
    
    @Bean public ObjectGraphAsListBuilder objectGraphListBuilder() {
        return new ObjectAsGraphListBuilderImpl(-1);
    }
    
    @Bean public SaveEntityAndChildrenIfAny saveEntityAndChildrenIfAny(
            @Autowired ModelObjectService modelObjectService) {
        return new SaveEntityAndChildrenIfAny(
                this.typeFromNameResolver(), 
                this.entityRepositoryProvider(),
                this.typeTests(),
                this.entityMapperService(),
                this.objectGraphListBuilder(),
                modelObjectService
        );
    }
    
    @Bean public FormSubmitHandler formSubmitHandler(
            @Autowired SaveEntityAndChildrenIfAny saveEntityAndChildrenIfAny) {
        return new FormSubmitHandlerImpl(
                saveEntityAndChildrenIfAny,
                this.typeFromNameResolver(), 
                this.entityRepositoryProvider()
        );
    }
    
    @Bean public FormValidatorFactory formValidatorFactory() {
        return new FormValidatorFactoryImpl(this.entityUniqueColumnsValidator());
    }
    
    @Bean public EntityUniqueColumnsValidator entityUniqueColumnsValidator() {
        return new EntityUniqueColumnsValidator(
                this.entityRepositoryProvider());
    }

    @Override 
    @Bean public TypeFromNameResolver typeFromNameResolver() {
        return new MappedEntityTypeFromNameResolver(
                this.entityMapperService(), 
                super.typeFromNameResolver());
    }
        
    @Bean public EntityRepositoryProvider entityRepositoryProvider() {
        return new MappedEntityRepositoryProvider(
                this.entityMapperService(), this.unMappedEntityRepositoryProvider());
    }

    private EntityRepositoryProvider unMappedEntityRepositoryProvider() {
        return new EntityRepositoryProviderImpl(
                this.jpaObjectFactory(), this.domainClasses());
    }
    
    private EntityMapperService entityMapperService() {
        return applicationContext.getBean(EntityMapperService.class);
    }
}
