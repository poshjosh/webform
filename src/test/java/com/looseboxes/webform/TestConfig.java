package com.looseboxes.webform;

import com.bc.jpa.spring.DomainClasses;
import com.bc.jpa.spring.DomainClassesBuilder;
import com.looseboxes.webform.store.PropertyStoreImpl;
import com.looseboxes.webform.util.PropertySearchImpl;
import com.looseboxes.webform.util.PropertySearch;
import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.jpa.spring.TypeFromNameResolverUsingClassNames;
import com.bc.webform.functions.TypeTests;
import com.bc.webform.functions.TypeTestsImpl;
import java.util.Properties;
import java.util.Set;
import org.springframework.context.annotation.Bean;

/**
 * @author hp
 */
public class TestConfig extends TestBase{
    
//    private static final Logger LOG = LoggerFactory.getLogger(TestConfig.class);
    
    @Bean public TypeTests typeTests() {
        return new TypeTestsImpl();
    }
    
    @Bean public PropertySearch propertyAccess(String prefix, Properties props) {
        return new PropertySearchImpl(prefix, 
                new PropertyStoreImpl(props), 
                this.typeFromNameResolver(),
                ""
        );
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
