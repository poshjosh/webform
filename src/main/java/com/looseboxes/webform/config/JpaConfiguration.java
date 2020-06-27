package com.looseboxes.webform.config;

import com.bc.jpa.spring.AbstractJpaConfiguration;
import com.looseboxes.webform.form.FormSubmitHandler;
import com.looseboxes.webform.form.FormSubmitHandlerImpl;
import com.looseboxes.webform.form.validators.EntityUniqueColumnsValidator;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.form.validators.FormValidatorFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import com.looseboxes.webform.entity.EntityRepositoryProvider;
import com.looseboxes.webform.entity.EntityRepositoryProviderImpl;

/**
 * @author hp
 */
public abstract class JpaConfiguration extends AbstractJpaConfiguration{
    
    @Bean @Scope("prototype") public FormSubmitHandler formSubmitHandler() {
        return new FormSubmitHandlerImpl(this.typeFromNameResolver(), 
                this.entityRepositoryProvider());
    }
    
    @Bean @Scope("prototype") public FormValidatorFactory formValidatorFactory() {
        return new FormValidatorFactoryImpl(this.entityUniqueColumnsValidator());
    }
    
    @Bean @Scope("prototype") 
    public EntityUniqueColumnsValidator entityUniqueColumnsValidator() {
        return new EntityUniqueColumnsValidator(
                this.entityRepositoryProvider());
    }
        
    @Bean @Scope("singleton") EntityRepositoryProvider entityRepositoryProvider() {
        return new EntityRepositoryProviderImpl(
                this.entityRepositoryFactory(), this.metaDataAccess());
    }
}
