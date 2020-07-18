package com.looseboxes.webform.config;

import com.bc.jpa.spring.AbstractJpaConfiguration;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.TypeTests;
import com.looseboxes.webform.configurers.EntityMapperService;
import com.looseboxes.webform.form.FormSubmitHandler;
import com.looseboxes.webform.form.FormSubmitHandlerImpl;
import com.looseboxes.webform.form.validators.EntityUniqueColumnsValidator;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.form.validators.FormValidatorFactoryImpl;
import org.springframework.context.annotation.Bean;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.repository.EntityRepositoryProviderImpl;
import com.looseboxes.webform.repository.MappedEntityRepositoryProvider;
import com.looseboxes.webform.repository.MappedEntityTypeFromNameResolver;
import com.looseboxes.webform.services.ModelObjectService;
import com.looseboxes.webform.util.ObjectAsGraphListBuilderImpl;
import com.looseboxes.webform.util.ObjectGraphAsListBuilder;
import com.looseboxes.webform.util.SaveEntityAndChildrenIfAny;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hp
 */
public abstract class AbstractWebformJpaConfiguration extends AbstractJpaConfiguration{
    
    @Autowired private TypeTests typeTests;
    @Autowired private EntityMapperService entityMapperService;
    
    @Bean public ObjectGraphAsListBuilder objectGraphListBuilder() {
        return new ObjectAsGraphListBuilderImpl(-1);
    }
    
    @Bean public SaveEntityAndChildrenIfAny saveEntityAndChildrenIfAny(
            ModelObjectService modelObjectService) {
        return new SaveEntityAndChildrenIfAny(
                this.typeFromNameResolver(), 
                this.entityRepositoryProvider(),
                this.typeTests,
                this.entityMapperService,
                this.objectGraphListBuilder(),
                modelObjectService
        );
    }
    
    @Bean public FormSubmitHandler formSubmitHandler(
            ModelObjectService modelObjectService) {
        return new FormSubmitHandlerImpl(
                this.saveEntityAndChildrenIfAny(modelObjectService),
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

    @Override public TypeFromNameResolver typeFromNameResolver() {
        return new MappedEntityTypeFromNameResolver(
                this.entityMapperService, 
                super.typeFromNameResolver());
    }
        
    @Bean public EntityRepositoryProvider entityRepositoryProvider() {
        return new MappedEntityRepositoryProvider(
                this.entityMapperService, this.unMappedEntityRepositoryProvider());
    }

    public EntityRepositoryProvider unMappedEntityRepositoryProvider() {
        return new EntityRepositoryProviderImpl(
                this.jpaObjectFactory(), this.domainClasses());
    }
}
