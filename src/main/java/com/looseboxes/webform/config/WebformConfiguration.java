package com.looseboxes.webform.config;

import com.looseboxes.webform.configurers.EntityConfigurerService;
import com.looseboxes.webform.configurers.EntityConfigurerServiceImpl;
import com.looseboxes.webform.store.EnvironmentStore;
import com.looseboxes.webform.store.PropertyStore;
import com.looseboxes.webform.util.PropertySearchImpl;
import com.looseboxes.webform.util.PropertySearch;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.form.FormBuilder;
import com.bc.webform.form.FormBuilderForJpaEntity;
import com.bc.webform.form.member.FormMemberBuilder;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.form.FormMemberComparatorImpl;
import com.looseboxes.webform.form.FormFieldTest;
import com.looseboxes.webform.form.FormFieldTestImpl;
import com.bc.webform.form.member.FormInputContext;
import com.bc.webform.form.member.MultiChoiceContext;
import com.bc.webform.form.member.ReferencedFormContext;
import com.bc.webform.TypeTests;
import com.bc.webform.TypeTestsImpl;
import com.looseboxes.webform.MessageAttributes;
import com.looseboxes.webform.MessageAttributesImpl;
import com.looseboxes.webform.WebformDefaults;
import com.looseboxes.webform.converters.DomainTypeConverter;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.configurers.EntityMapperService;
import com.looseboxes.webform.configurers.EntityMapperServiceImpl;
import com.looseboxes.webform.form.DependentsProvider;
import com.looseboxes.webform.form.DependentsProviderImpl;
import com.looseboxes.webform.form.FormInputContextWithDefaultValuesFromProperties;
import com.looseboxes.webform.form.FormMemberBuilderImpl;
import com.looseboxes.webform.form.MultiChoiceContextImpl;
import java.lang.reflect.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import com.looseboxes.webform.form.FormMemberComparator;
import com.looseboxes.webform.util.TextExpressionMethodsImpl;
import com.looseboxes.webform.form.ReferencedFormContextImpl;
import com.looseboxes.webform.util.TextExpressionResolverImpl;
import com.looseboxes.webform.util.TextExpressionMethods;
import com.looseboxes.webform.util.TextExpressionResolver;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.form.FormMemberUpdater;
import com.looseboxes.webform.form.FormMemberUpdaterImpl;
import com.looseboxes.webform.form.UpdateParentFormWithNewlyCreatedModel;
import com.looseboxes.webform.util.PropertySuffixes;

/**
 * @author hp
 */
@Configuration
@ComponentScan(basePackages = {"com.looseboxes.webform"})
@PropertySource("classpath:webform.properties")
public class WebformConfiguration {
    
//    private final Logger log = LoggerFactory.getLogger(WebformConfiguration.class);
    
    @Autowired private Environment environment;
    
    public WebformConfiguration() { }

    @Bean public EntityMapperService 
        entityMapperService(ApplicationContext applicationContext) {
        EntityMapperService service = new EntityMapperServiceImpl();
        try{
            WebformConfigurer configurer = applicationContext.getBean(WebformConfigurer.class);
            configurer.addEntityMappers(service);
        }catch(NoSuchBeanDefinitionException ignored) { }
        return service;
    }
    
    @Bean public EntityConfigurerService 
        entityConfigurerService(ApplicationContext applicationContext) {
        EntityConfigurerService service = new EntityConfigurerServiceImpl();
        try{
            WebformConfigurer configurer = applicationContext.getBean(WebformConfigurer.class);
            configurer.addEntityConfigurers(service);
        }catch(NoSuchBeanDefinitionException ignored) { }
        return service;
    }
    
    @Bean public MessageAttributes messageAttributes() {
        return new MessageAttributesImpl();
    }
    
    @Bean public FormBuilder formBuilder(
            @Autowired PropertySearch propertySearch,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired TemporalToStringConverter temporalToStringConverter,
            @Autowired DomainTypeToIdConverter entityToIdConverter,
            @Autowired TypeFromNameResolver typeFromNameResolver) {
        return new FormBuilderForJpaEntity()
                .sourceFieldsProvider(this.formFieldTest(propertySearch))
                .formMemberComparator(this.formMemberComparator(propertySearch))
                .formMemberBuilder(
                        this.formMemberBuilder(
                                propertySearch, dateToStringConverter, 
                                temporalToStringConverter, entityToIdConverter,
                                typeFromNameResolver)
                );
                
    }
    
    @Bean public FormMemberBuilder<Object, Field, Object> formMemberBuilder(
            @Autowired PropertySearch propertySearch,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired TemporalToStringConverter temporalToStringConverter,
            @Autowired DomainTypeToIdConverter entityToIdConverter,
            @Autowired TypeFromNameResolver typeFromNameResolver) {
        
        final FormInputContext<Object, Field, Object> formInputContext = 
                formInputContext(propertySearch, dateToStringConverter, 
                        temporalToStringConverter, entityToIdConverter);
        
        return new FormMemberBuilderImpl(
                propertySearch, 
                formInputContext, 
                this.referencedFormContext(typeFromNameResolver)
                
        );
    }

    @Bean public ReferencedFormContext<Object, Field> referencedFormContext(
            @Autowired TypeFromNameResolver typeFromNameResolver) {
        
        return new ReferencedFormContextImpl(typeTests(), typeFromNameResolver);
    }
    
    @Bean public UpdateParentFormWithNewlyCreatedModel updateParentFormWithNewlyCreatedModel(
            FormMemberUpdater formMemberUpdater) {
        return new UpdateParentFormWithNewlyCreatedModel(formMemberUpdater);
    }

    @Bean public FormMemberUpdater formMemberUpdater(
            @Autowired FormInputContext formInputContext) {
        return new FormMemberUpdaterImpl(formInputContext);
    }
    
    @Bean public FormInputContext<Object, Field, Object> formInputContext(
            @Autowired PropertySearch propertySearch,
            @Autowired DateToStringConverter dateToStringConverter,
            @Autowired TemporalToStringConverter temporalToStringConverter,
            @Autowired DomainTypeToIdConverter domainTypeToIdConverter) {
        
        return new FormInputContextWithDefaultValuesFromProperties(
                this.typeTests(),
                propertySearch,
                this.propertyExpressionsResolver(),
                dateToStringConverter,
                temporalToStringConverter,
                domainTypeToIdConverter);
    }

    @Bean public TextExpressionResolver propertyExpressionsResolver() {
        return new TextExpressionResolverImpl(
                this.propertyExpressionMethods()
        );
    }
    
    @Bean public TextExpressionMethods propertyExpressionMethods() {
        return new TextExpressionMethodsImpl();
    }
    
    @Bean public MultiChoiceContext<Object, Field> multiChoiceContext(
            @Autowired DomainObjectPrinter printer) {
        return new MultiChoiceContextImpl(
                this.typeTests(),
                printer,
                WebformDefaults.LOCALE
        );
    }
    
    @Bean public DependentsProvider dependentsProvider(
            @Autowired EntityRepositoryProvider repoFactory,
            @Autowired PropertySuffixes propertySuffixes,
            @Autowired DomainTypeConverter domainTypeConverter,
            @Autowired DomainObjectPrinter domainObjectPrinter) {
        return new DependentsProviderImpl(
                this.propertySearch(propertySuffixes), 
                repoFactory, 
                this.typeTests(),
                domainTypeConverter,
                domainObjectPrinter);
    }
    
    @Bean public FormFieldTest formFieldTest(
            @Autowired PropertySearch propertySearch) {
        return new FormFieldTestImpl(propertySearch, this.typeTests());
    }
    
    @Bean public FormMemberComparator formMemberComparator(
            @Autowired PropertySearch propertySearch) {
        return new FormMemberComparatorImpl(propertySearch);
    }
    
    @Bean public TypeTests typeTests() {
        return new TypeTestsImpl();
    }
    
    @Bean public PropertySearch propertySearch(
            @Autowired PropertySuffixes propertySuffixes) {
        return new PropertySearchImpl(environmentStore(), propertySuffixes);
    }
    
    @Bean public PropertySuffixes propertySuffixes(
            @Autowired TypeFromNameResolver typeFromNameResolver) {
        return new PropertySuffixes(typeFromNameResolver);
    }

    @Bean public PropertyStore environmentStore() {
        return new EnvironmentStore(this.environment);
    }
}
