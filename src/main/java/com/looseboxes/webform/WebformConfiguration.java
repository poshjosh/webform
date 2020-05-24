package com.looseboxes.webform;

import com.looseboxes.webform.form.FormFactoryImpl;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.store.EnvironmentStore;
import com.looseboxes.webform.store.PropertyStore;
import com.looseboxes.webform.util.PropertySearchImpl;
import com.looseboxes.webform.util.PropertySearch;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.FormBuilder;
import com.bc.webform.FormBuilderForJpaEntity;
import com.bc.webform.FormMemberBuilder;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.converters.EntityToIdConverter;
import com.looseboxes.webform.form.FormMemberComparatorImpl;
import com.looseboxes.webform.form.FormFieldTest;
import com.looseboxes.webform.form.FormFieldTestImpl;
import com.bc.webform.functions.FormInputContext;
import com.bc.webform.functions.FormInputNameProvider;
import com.bc.webform.functions.MultiChoiceContext;
import com.bc.webform.functions.ReferencedFormContext;
import com.bc.webform.functions.TypeTests;
import com.bc.webform.functions.TypeTestsImpl;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.form.FormInputContextImpl;
import com.looseboxes.webform.form.FormMemberBuilderImpl;
import com.looseboxes.webform.form.MultiChoiceContextImpl;
import java.lang.reflect.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import com.looseboxes.webform.form.FormMemberComparator;
import com.looseboxes.webform.form.PropertyExpressionsResolver;
import com.looseboxes.webform.form.PropertyExpressionsResolverImpl;
import com.looseboxes.webform.form.ReferencedFormContextImpl;

/**
 * @author hp
 */
@Configuration
@ComponentScan(basePackages = {"com.looseboxes.webform"})
@PropertySource("classpath:webform.properties")
public class WebformConfiguration {
    
//    private static final Logger LOG = LoggerFactory.getLogger(WebformConfiguration.class);
    
    @Autowired private Environment environment;
    
    public WebformConfiguration() { }

    @Bean @Scope("prototype") public FormFactory formFactory(
            @Autowired TypeFromNameResolver typeFromNameResolver,
            @Autowired PropertySearch propertySearch,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired TemporalToStringConverter temporalToStringConverter,
            @Autowired EntityToIdConverter entityToIdConverter) {
        
        return new FormFactoryImpl(
                typeFromNameResolver, 
                this.formBuilder(propertySearch, dateToStringConverter, 
                        temporalToStringConverter, entityToIdConverter));
    }

    @Bean @Scope("prototype") public FormBuilder formBuilder(
            @Autowired PropertySearch propertySearch,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired TemporalToStringConverter temporalToStringConverter,
            @Autowired EntityToIdConverter entityToIdConverter) {
        return new FormBuilderForJpaEntity()
                .sourceFieldsProvider(this.formFieldTest(propertySearch))
                .formMemberComparator(this.formMemberComparator(propertySearch))
                .formMemberBuilder(
                        this.formMemberBuilder(
                                propertySearch, dateToStringConverter, 
                                temporalToStringConverter, entityToIdConverter)
                );
                
    }

    @Bean @Scope("prototype") public FormMemberBuilder<Object, Field, Object> formMemberBuilder(
            @Autowired PropertySearch propertySearch,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired TemporalToStringConverter temporalToStringConverter,
            @Autowired EntityToIdConverter entityToIdConverter) {
        
        final FormInputContext<Object, Field, Object> formInputContext = 
                formInputContext(propertySearch, dateToStringConverter, 
                        temporalToStringConverter, entityToIdConverter);
        
        return new FormMemberBuilderImpl(
                propertySearch, 
                formInputContext, 
                this.referencedFormContext(formInputContext)
                
        );
    }

    @Bean @Scope("prototype") public ReferencedFormContext<Object, Field> referencedFormContext(
            @Autowired FormInputNameProvider<Object, Field> formInputNameProvider) {
        
        return new ReferencedFormContextImpl(typeTests(), formInputNameProvider);
    }

    @Bean @Scope("prototype") public FormInputContext<Object, Field, Object> formInputContext(
            @Autowired PropertySearch propertySearch,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired TemporalToStringConverter temporalToStringConverter,
            @Autowired EntityToIdConverter entityToIdConverter) {
        
        return new FormInputContextImpl(
                this.typeTests(),
                propertySearch,
                this.propertyExpressionsResolver(),
                dateToStringConverter,
                temporalToStringConverter,
                entityToIdConverter);
    }
    
    @Bean public PropertyExpressionsResolver propertyExpressionsResolver() {
        return new PropertyExpressionsResolverImpl();
    }
    
    @Bean @Scope("prototype") public MultiChoiceContext<Object, Field> multiChoiceContext(
            @Autowired EntityRepositoryFactory repoFactory,
            @Autowired TypeFromNameResolver typeFromNameResolver,
            @Autowired FormInputContext<Object, Field, Object> formInputContext,
            @Autowired DomainObjectPrinter printer) {
        return new MultiChoiceContextImpl(
                this.typeTests(),
                repoFactory,
                this.propertySearch(typeFromNameResolver),
                formInputContext,
                printer,
                WebformDefaults.LOCALE
        );
    }
    
    @Bean @Scope("prototype") public FormFieldTest formFieldTest(
            @Autowired PropertySearch propertySearch) {
        return new FormFieldTestImpl(propertySearch, this.typeTests());
    }
    
    @Bean @Scope("prototype") public FormMemberComparator formMemberComparator(
            @Autowired PropertySearch propertySearch) {
        return new FormMemberComparatorImpl(propertySearch);
    }

    @Bean @Scope("prototype") public TypeTests typeTests() {
        return new TypeTestsImpl();
    }
    
    @Bean @Scope("prototype") public PropertySearch propertySearch(
            @Autowired TypeFromNameResolver typeFromNameResolver) {
        return new PropertySearchImpl(environmentStore(), typeFromNameResolver);
    }
    
    @Bean public PropertyStore environmentStore() {
        return new EnvironmentStore(this.environment);
    }
}
