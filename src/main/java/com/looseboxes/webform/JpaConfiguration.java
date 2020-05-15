package com.looseboxes.webform;

import com.bc.jpa.spring.AbstractJpaConfiguration;
import com.looseboxes.webform.form.validators.EntityUniqueColumnsValidator;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.form.validators.FormValidatorFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * @author hp
 */
public abstract class JpaConfiguration extends AbstractJpaConfiguration{

    @Bean @Scope("prototype") public FormController.OnFormSubmitted onFormSubmitted() {
        return new OnFormSubmittedImpl(
                this.typeFromNameResolver(),
                this.entityRepositoryFactory());
    }
    
    @Bean @Scope("prototype") public FormValidatorFactory formValidatorFactory() {
        return new FormValidatorFactoryImpl(this.entityUniqueColumnsValidator());
    }
    
    @Bean @Scope("prototype") public EntityUniqueColumnsValidator 
        entityUniqueColumnsValidator() {
        return new EntityUniqueColumnsValidator(
                this.metaDataAccess(),
                this.entityRepositoryFactory());
    }
}
