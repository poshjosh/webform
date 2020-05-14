package com.looseboxes.webform;

import com.looseboxes.webform.form.OnFormSubmittedImpl;
import com.bc.jpa.spring.AbstractJpaConfiguration;
import com.looseboxes.webform.form.validators.EntityUniqueColumnsValidator;
import com.looseboxes.webform.form.validators.FormValidatorFactory;
import com.looseboxes.webform.form.validators.FormValidatorFactoryImpl;
import javax.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author hp
 */
@Configuration
public class JpaConfiguration extends AbstractJpaConfiguration{
    
    @Autowired private EntityManagerFactory entityMangerFactory;

    @Override
    public EntityManagerFactory entityManagerFactory() {
        return this.entityMangerFactory;
    }
    
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
