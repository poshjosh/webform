package com.looseboxes.webform;

import com.bc.jpa.spring.DomainClasses;
import com.bc.jpa.spring.DomainClassesBuilder;
import com.looseboxes.webform.store.PropertyStoreImpl;
import com.looseboxes.webform.util.PropertySearchImpl;
import com.looseboxes.webform.util.PropertySearch;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.TypeFromNameResolverUsingClassNames;
import com.bc.webform.TypeTests;
import com.bc.webform.TypeTestsImpl;
import com.looseboxes.webform.services.FormAttributeService;
import com.looseboxes.webform.store.AttributeStoreProvider;
import com.looseboxes.webform.store.StoreConfiguration;
import com.looseboxes.webform.util.PropertySuffixes;
import java.util.Properties;
import java.util.Set;
import org.springframework.context.annotation.Bean;

/**
 * @author hp
 */
public class TestConfig extends TestBase{
    
//    private static final Logger LOG = LoggerFactory.getLogger(TestConfig.class);
    
    @Bean public FormAttributeService getFormAttributeService() {
        return new FormAttributeService(this.getAttributeStoreProvider());
    }
    
    @Bean public AttributeStoreProvider getAttributeStoreProvider() {
        return this.getStoreConfiguration().attributeStoreProvider();
    }
    
    @Bean public StoreConfiguration getStoreConfiguration() {
        return new StoreConfiguration();
    }
    
    @Bean public TypeTests typeTests() {
        return new TypeTestsImpl();
    }
    
    @Bean public PropertySearch propertySearch(String prefix, Properties props, String separator) {
        return new PropertySearchImpl(prefix, 
                new PropertyStoreImpl(props), 
                this.propertySuffixes(),
                separator
        );
    }
    
    @Bean public PropertySuffixes propertySuffixes() {
        return new PropertySuffixes(this.typeFromNameResolver());
    }
    
    @Bean public TypeFromNameResolver typeFromNameResolver() {
        final Set<Class> classes = this.domainClasses().get();
        return new TypeFromNameResolverUsingClassNames(classes);
    }

    @Bean DomainClasses domainClasses() {
        return this.domainClassesBuilder()
                .reset()
//                .addFrom(this.entityManagerFactory())
                .addFromPersistenceXmlFile()
                .addFromPackages(this.getEntityPackageNames())
                .build();
    }

    public String [] getEntityPackageNames() {
        return new String[0];
    }

    @Bean DomainClassesBuilder domainClassesBuilder() {
        return new DomainClasses.Builder();
    }
}
