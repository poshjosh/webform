package com.looseboxes.webform.config;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.TypeTests;
import com.bc.webform.form.builder.FormBuilder;
import com.bc.webform.form.builder.FormBuilderForJpaEntity;
import com.bc.webform.form.member.FormInputContext;
import com.bc.webform.form.member.builder.FormMemberBuilder;
import com.bc.webform.form.member.MultiChoiceContext;
import com.bc.webform.form.member.ReferencedFormContext;
import com.looseboxes.webform.WebformLocaleSupplier;
import com.looseboxes.webform.configurers.EntityConfigurerService;
import com.looseboxes.webform.configurers.EntityConfigurerServiceImpl;
import com.looseboxes.webform.mappers.EntityMapperService;
import com.looseboxes.webform.mappers.EntityMapperServiceImpl;
import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.converters.DomainTypeConverter;
import com.looseboxes.webform.converters.DomainTypeToIdConverter;
import com.looseboxes.webform.converters.TemporalToStringConverter;
import com.looseboxes.webform.form.DependentsProvider;
import com.looseboxes.webform.form.DependentsProviderImpl;
import com.looseboxes.webform.converters.EntityToSelectOptionConverter;
import com.looseboxes.webform.form.FormBuilderProvider;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.form.FormFactoryImpl;
import com.looseboxes.webform.form.FormFieldTest;
import com.looseboxes.webform.form.FormFieldTestImpl;
import com.looseboxes.webform.form.FormInputContextWithDefaultValuesFromProperties;
import com.looseboxes.webform.form.FormMemberBuilderImpl;
import com.looseboxes.webform.form.FormMemberComparator;
import com.looseboxes.webform.form.FormMemberComparatorImpl;
import com.looseboxes.webform.form.FormMemberUpdater;
import com.looseboxes.webform.form.FormMemberUpdaterImpl;
import com.looseboxes.webform.form.ReferencedFormContextImpl;
import com.looseboxes.webform.form.UpdateParentFormWithNewlyCreatedModel;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import com.looseboxes.webform.store.EnvironmentStore;
import com.looseboxes.webform.store.PropertyStore;
import com.looseboxes.webform.util.PropertySearch;
import com.looseboxes.webform.util.PropertySearchImpl;
import com.looseboxes.webform.util.PropertySuffixes;
import com.looseboxes.webform.util.TextExpressionMethods;
import com.looseboxes.webform.util.TextExpressionMethodsImpl;
import com.looseboxes.webform.util.TextExpressionResolver;
import com.looseboxes.webform.util.TextExpressionResolverImpl;
import com.looseboxes.webform.web.BindingResultErrorCollector;
import java.lang.reflect.Field;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import com.looseboxes.webform.converters.DomainTypePrinter;
import com.looseboxes.webform.form.MultiChoiceContextImpl;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import com.looseboxes.webform.converters.IdStringToDomainTypeConverterFactory;

/**
 * @author hp
 */
public class WebformConfigurationSource {
    
    private final Logger log = LoggerFactory.getLogger(WebformConfigurationSource.class);
    
    private final ApplicationContext applicationContext;
    
    public WebformConfigurationSource(ApplicationContext applicationContext) { 
        this.applicationContext = Objects.requireNonNull(applicationContext);
    }
    
    @Bean public BindingResultErrorCollector bindingResultErrorCollector() {
        return new BindingResultErrorCollector();
    }
    
    @Bean public EntityMapperService entityMapperService() {
        EntityMapperService service = new EntityMapperServiceImpl();
        try{
            WebformConfigurer configurer = applicationContext.getBean(WebformConfigurer.class);
            configurer.addEntityMappers(service);
        }catch(NoSuchBeanDefinitionException ignored) { }
        return service;
    }
    
    @Bean public EntityConfigurerService entityConfigurerService() {
        EntityConfigurerService service = new EntityConfigurerServiceImpl();
        try{
            WebformConfigurer configurer = applicationContext.getBean(WebformConfigurer.class);
            configurer.addEntityConfigurers(service);
        }catch(NoSuchBeanDefinitionException ignored) { }
        return service;
    }
        
    
    @Bean public FormFactory formFactory() {
        return new FormFactoryImpl(formBuilderProvider());
    }
    
    
    @Bean public FormBuilderProvider formBuilderProvider() {
        // We need each call to return a new FormBuilder
        // builders may only be used once
        return () -> this.newFormBuilder();
    }

    @Bean public FormBuilder<Object, Field, Object> formBuilder() {
        return this.newFormBuilder();
    }
    
    private FormBuilder<Object, Field, Object> newFormBuilder() {
        return new FormBuilderForJpaEntity(typeTests())
                .sourceFieldsProvider(formFieldTest())
                .formMemberBuilderProvider(() -> this.newFormMemberBuilder())
                .formMemberComparator(formMemberComparator());
    }

    @Bean public FormMemberBuilder<Object, Field, Object> formMemberBuilder() {
        return this.newFormMemberBuilder();
    }

    private FormMemberBuilder<Object, Field, Object> newFormMemberBuilder() {
        return new FormMemberBuilderImpl(propertySearch())
                .formInputContext(formInputContext())
                .multiChoiceContext(multiChoiceContext())
                .referencedFormContext(referencedFormContext());
    }
    
    @Bean public ReferencedFormContext<Object, Field> referencedFormContext() {
        return new ReferencedFormContextImpl(typeTests(), typeFromNameResolver());
    }
    
    private TypeFromNameResolver typeFromNameResolver() {
        return this.applicationContext.getBean(TypeFromNameResolver.class);
    }
    
    @Bean public UpdateParentFormWithNewlyCreatedModel updateParentFormWithNewlyCreatedModel() {
        return new UpdateParentFormWithNewlyCreatedModel(formMemberUpdater());
    }

    @Bean public FormMemberUpdater formMemberUpdater() {
        return new FormMemberUpdaterImpl(formInputContext());
    }
    
    @Bean public FormInputContext<Object, Field, Object> formInputContext() {
        return new FormInputContextWithDefaultValuesFromProperties(
                typeTests(),
                propertySearch(),
                textExpressionsResolver(),
                dateToStringConverter(),
                temporalToStringConverter(),
                domainTypeToIdConverter(),
                domainTypeConverter(),
                domainTypePrinter());
    }
    
    private DateToStringConverter dateToStringConverter() {
        return this.applicationContext.getBean(DateToStringConverter.class);
    }
    
    private TemporalToStringConverter temporalToStringConverter() {
        return this.applicationContext.getBean(TemporalToStringConverter.class);
    }
    
    private DomainTypeToIdConverter domainTypeToIdConverter() {
        return this.applicationContext.getBean(DomainTypeToIdConverter.class);
    }

    @Bean public TextExpressionResolver textExpressionsResolver() {
        return new TextExpressionResolverImpl(
                this.propertyExpressionMethods()
        );
    }
    
    @Bean public TextExpressionMethods propertyExpressionMethods() {
        return new TextExpressionMethodsImpl();
    }
    
    @Bean public MultiChoiceContext<Object, Field> multiChoiceContext() {
        Locale locale = WebformLocaleSupplier.getLocale(applicationContext);
        return new MultiChoiceContextImpl(this.typeTests(), locale, 
                this.idToDomainTypeConverterFactory(), 
                this.entityToSelectOptionConverter());
    }
    
    private IdStringToDomainTypeConverterFactory idToDomainTypeConverterFactory() {
        return applicationContext.getBean(IdStringToDomainTypeConverterFactory.class);
    }
    
    @Bean public DependentsProvider dependentsProvider() {
        return new DependentsProviderImpl(
                propertySearch(), 
                entityRepositoryProvider(), 
                typeTests(),
                domainTypeConverter(),
                domainTypePrinter(),
                entityToSelectOptionConverter());
    }
    
    private EntityRepositoryProvider entityRepositoryProvider() {
        return this.applicationContext.getBean(EntityRepositoryProvider.class);
    }
    
    private DomainTypeConverter domainTypeConverter() {
        return this.applicationContext.getBean(DomainTypeConverter.class);
    }
    
    private DomainTypePrinter domainTypePrinter() {
        return this.applicationContext.getBean(DomainTypePrinter.class);
    }
    
    private EntityToSelectOptionConverter entityToSelectOptionConverter() {
        return this.applicationContext.getBean(EntityToSelectOptionConverter.class);
    }

    @Bean public FormFieldTest formFieldTest() {
        return new FormFieldTestImpl(propertySearch(), typeTests());
    }
    
    private TypeTests typeTests() {
        return applicationContext.getBean(TypeTests.class);
    }
    
    
    @Bean public FormMemberComparator formMemberComparator() {
        return new FormMemberComparatorImpl(propertySearch());
    }
    
    @Bean public PropertySearch propertySearch() {
        return new PropertySearchImpl(environmentStore(), propertySuffixes());
    }
    
    @Bean public PropertySuffixes propertySuffixes() {
        return new PropertySuffixes(this.getTypeFromNameResolver());
    }
    
    private TypeFromNameResolver getTypeFromNameResolver(){
        return applicationContext.getBean(TypeFromNameResolver.class);
    }

    @Bean public PropertyStore environmentStore() {
        return new EnvironmentStore(this.getEnvironment());
    }
    
    private Environment getEnvironment() {
        return this.getApplicationContext().getEnvironment();
    }
    
    private ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }
}
