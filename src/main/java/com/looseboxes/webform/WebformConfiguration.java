package com.looseboxes.webform;

import com.looseboxes.webform.form.FormFactoryImpl;
import com.looseboxes.webform.form.FormFieldsCreatorImpl;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.form.FormFieldChoicesImpl;
import com.looseboxes.webform.form.FormFieldChoices;
import com.looseboxes.webform.store.EnvironmentStore;
import com.looseboxes.webform.store.PropertyStore;
import com.looseboxes.webform.store.PropertySearchImpl;
import com.looseboxes.webform.store.PropertySearch;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.Form;
import com.bc.webform.FormBuilder;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.converters.EntityToIdConverter;
import com.looseboxes.webform.form.FormFieldComparator;
import com.looseboxes.webform.form.FormFieldComparatorImpl;
import com.looseboxes.webform.form.FormFieldTest;
import com.looseboxes.webform.form.FormFieldTestImpl;
import com.bc.webform.functions.FormFieldsCreator;
import com.bc.webform.functions.TypeTests;
import com.bc.webform.functions.TypeTestsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

/**
 * @author hp
 */
@Configuration
@PropertySource("classpath:webform.properties")
public class WebformConfiguration {
    
//    private static final Logger LOG = LoggerFactory.getLogger(WebformConfiguration.class);
    
    @Autowired private Environment environment;
    
    public WebformConfiguration() { }

    @Bean @Scope("prototype") public TypeTests typeTests() {
        return new TypeTestsImpl();
    }
    
    @Bean @Scope("prototype") public PropertySearch propertySearch(
            @Autowired TypeFromNameResolver typeFromNameResolver) {
        return new PropertySearchImpl(
                WebformProperties.PROPERTY_PREFIX, 
                this.environmentStore(), 
                typeFromNameResolver);
    }
    
    @Bean public PropertyStore environmentStore() {
        return new EnvironmentStore(this.environment);
    }
    
    @Bean @Scope("prototype") public FormFactory formFactory(
            @Autowired TypeFromNameResolver typeFromNameResolver,
            @Autowired PropertySearch propertySearch,
            @Autowired FormFieldChoices formFieldChoices,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired EntityToIdConverter entityToIdConverter) {
        return new FormFactoryImpl(
                this.formBuilder(),
                typeFromNameResolver,
                this.formFieldsCreator(
                        propertySearch, formFieldChoices,
                        dateToStringConverter, entityToIdConverter),
                this.formFieldComparator()
        );
    }

    @Bean @Scope("prototype") public FormFieldsCreator formFieldsCreator(
            @Autowired PropertySearch propertySearch,
            @Autowired FormFieldChoices formFieldChoices,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired EntityToIdConverter entityToIdConverter) {
        
        return new FormFieldsCreatorImpl(
                this.isFormField(propertySearch),
                propertySearch,
                formFieldChoices,
                this.formFieldComparator(),
                dateToStringConverter,
                entityToIdConverter
        );
// Other options    
//        new CreateFormFieldsFromAnnotatedPersistenceEntity(this.isFormField(), -1); 
//        return new CreateFormFieldsFromObject();
//        final EntityManagerFactory emf;
//        return new CreateFormFieldsFromDatabaseTable(emf, new ColumnNameIsFormFieldTest(emf));
    }

    @Bean @Scope("prototype") public FormFieldChoices formFieldChoices(
            @Autowired EntityRepositoryFactory repoFactory,
            @Autowired TypeFromNameResolver typeFromNameResolver,
            @Autowired DomainObjectPrinter printer) {
        return new FormFieldChoicesImpl(
                this.typeTests(),
                repoFactory,
                this.propertySearch(typeFromNameResolver),
                printer,
                WebformDefaults.LOCALE
        );
    }
    
    @Bean @Scope("prototype") public FormBuilder formBuilder() {
        return new Form.Builder();
    }

    @Bean @Scope("prototype") public FormFieldTest isFormField(
            @Autowired PropertySearch propertySearch) {
        return new FormFieldTestImpl(propertySearch);
    }
    
    @Bean @Scope("prototype") public FormFieldComparator formFieldComparator() {
        return new FormFieldComparatorImpl();
    }
}
