package com.looseboxes.webform.config;

import com.bc.webform.form.FormBuilder;
import com.bc.webform.form.member.FormInputContext;
import com.bc.webform.form.member.FormMemberBuilder;
import com.bc.webform.form.member.MultiChoiceContext;
import com.bc.webform.form.member.ReferencedFormContext;
import com.looseboxes.webform.configurers.EntityConfigurerService;
import com.looseboxes.webform.configurers.EntityMapperService;
import com.looseboxes.webform.form.DependentsProvider;
import com.looseboxes.webform.form.FormBuilderProvider;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.form.FormFieldTest;
import com.looseboxes.webform.form.FormMemberComparator;
import com.looseboxes.webform.form.FormMemberUpdater;
import com.looseboxes.webform.form.UpdateParentFormWithNewlyCreatedModel;
import com.looseboxes.webform.store.PropertyStore;
import com.looseboxes.webform.util.PropertySearch;
import com.looseboxes.webform.util.PropertySuffixes;
import com.looseboxes.webform.util.TextExpressionMethods;
import com.looseboxes.webform.util.TextExpressionResolver;
import com.looseboxes.webform.web.BindingResultErrorCollector;
import java.lang.reflect.Field;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author hp
 */
@Configuration
@ComponentScan(basePackages = {"com.looseboxes.webform"})
@PropertySource("classpath:webform.properties")
@ConditionalOnMissingBean(WebformConfigurationSource.class)
public class WebformConfiguration{
    
//    private final Logger log = LoggerFactory.getLogger(WebformConfiguration.class);
    
    private final WebformConfigurationSource delegate;
    
    public WebformConfiguration(ApplicationContext applicationContext) { 
        delegate = new WebformConfigurationSource(applicationContext);
    }

    @Bean public BindingResultErrorCollector bindingResultErrorCollector() {
        return delegate.bindingResultErrorCollector();
    }

    @Bean public EntityMapperService entityMapperService() {
        return delegate.entityMapperService();
    }

    @Bean public EntityConfigurerService entityConfigurerService() {
        return delegate.entityConfigurerService();
    }

    @Bean public FormFactory formFactory() {
        return delegate.formFactory();
    }

    @Bean public FormBuilderProvider formBuilderProvider() {
        return delegate.formBuilderProvider();
    }

    @Bean public FormBuilder<Object, Field, Object> formBuilder() {
        return delegate.formBuilder();
    }

    @Bean public FormMemberBuilder<Object, Field, Object> formMemberBuilder() {
        return delegate.formMemberBuilder();
    }

    @Bean public ReferencedFormContext<Object, Field> referencedFormContext() {
        return delegate.referencedFormContext();
    }

    @Bean public UpdateParentFormWithNewlyCreatedModel updateParentFormWithNewlyCreatedModel() {
        return delegate.updateParentFormWithNewlyCreatedModel();
    }

    @Bean public FormMemberUpdater formMemberUpdater() {
        return delegate.formMemberUpdater();
    }

    @Bean public FormInputContext<Object, Field, Object> formInputContext() {
        return delegate.formInputContext();
    }

    @Bean public TextExpressionResolver textExpressionsResolver() {
        return delegate.textExpressionsResolver();
    }

    @Bean public TextExpressionMethods propertyExpressionMethods() {
        return delegate.propertyExpressionMethods();
    }

    @Bean public MultiChoiceContext<Object, Field> multiChoiceContext() {
        return delegate.multiChoiceContext();
    }

    @Bean public DependentsProvider dependentsProvider() {
        return delegate.dependentsProvider();
    }

    @Bean public FormFieldTest formFieldTest() {
        return delegate.formFieldTest();
    }

    @Bean public FormMemberComparator formMemberComparator() {
        return delegate.formMemberComparator();
    }

    @Bean public PropertySearch propertySearch() {
        return delegate.propertySearch();
    }

    @Bean public PropertySuffixes propertySuffixes() {
        return delegate.propertySuffixes();
    }

    @Bean public PropertyStore environmentStore() {
        return delegate.environmentStore();
    }
}
