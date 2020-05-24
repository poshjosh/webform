package com.looseboxes.webform.util;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.jpa.spring.DomainClasses;
import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

/**
 * @author hp
 */
public class PrintAppInfo extends Print implements CommandLineRunner{

    private static final Logger LOG = LoggerFactory.getLogger(PrintAppInfo.class);
    
    private final ApplicationContext context;
    
    public PrintAppInfo(ApplicationContext context) {
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public void run(String...args){

        System.out.println();
        LOG.debug("Printing command line arguments.");
        LOG.debug(args == null ? "null" : Arrays.toString(args));
        
        if( ! LOG.isTraceEnabled()) {
            return;
        }
        
        System.out.println();
        LOG.trace("Printing the beans provided by Spring Boot.");
        final String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        LOG.trace(Arrays.asList(beanNames).stream().collect(Collectors.joining("\n", "\n", "")));

        final EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);
        System.out.println();
        LOG.trace("Printing entity manager factory properties.");
        LOG.trace(this.toString(emf.getProperties()));
        
        final Metamodel metamodel = emf.getMetamodel();
        
        System.out.println();
        LOG.trace("Printing Embeddables");
        metamodel.getEmbeddables().stream()
                .map((e) -> println(e)).forEach((line) -> {
                    System.out.println(line);
                });

        System.out.println();
        LOG.trace("Printing Entities");
        metamodel.getEntities().stream()
                .map((e) -> println(e)).forEach((line) -> {
                    System.out.println(line);
                });

        System.out.println();
        LOG.debug("Printing entity manager properties.");
        final EntityManager em = emf.createEntityManager();
        try{
            LOG.debug(this.toString(em.getProperties()));
        }finally{
            em.close();
        }
        
        System.out.println();
        LOG.debug("Printing domain class names.");
        final DomainClasses domainClasses = context.getBean(DomainClasses.class);
        final Set<Class> classes = domainClasses.get();
        LOG.debug(classes.stream()
                .map(cls -> cls.getName()).collect(Collectors.joining("\n", "\n", "")));
        
        System.out.println();
        LOG.debug("Printing database table and column names.");
        final MetaDataAccess mda = context.getBean(MetaDataAccess.class);
        new com.bc.jpa.spring.PrintDatabaseTablesAndColumns(mda).run();
        
        System.out.println();
        LOG.debug("Printing database table row counts");
        final EntityRepositoryFactory repoFactory = context
                .getBean(EntityRepositoryFactory.class);
        final StringBuilder builder = new StringBuilder();
        for(Class cls : classes) {
            final EntityRepository repo = repoFactory.forEntity(cls);
            final long count = repo.count();
            builder.append('\n').append('\n').append(cls.getSimpleName())
                    .append(" has ").append(count).append(" records");
            if(LOG.isTraceEnabled()) {
                final List entities = repo.findAll(0, 1000);
                for(Object e : entities) {
                    System.out.println(e);
                }
            }
            
        }
        LOG.debug(builder.toString());
    }
    
    private String println(EntityType t) {
        final StringBuilder b = new StringBuilder();
        this.append(t, b);
        return b.toString();
    }
    
    private void append(EntityType t, StringBuilder b) {
        b.append('\n').append("name=").append(t.getName());
        this.append((ManagedType)t, b);
    }
    
    private String println(ManagedType t) {
        final StringBuilder b = new StringBuilder();
        this.append(t, b);
        return b.toString();
    }

    private void append(ManagedType t, StringBuilder b) {
        final Set<Attribute> attrSet = t.getAttributes();
        for(Attribute a : attrSet) {
            b.append("\nAttribute{name=").append(a.getName())
                    .append(", type=").append(a.getPersistentAttributeType())
                    .append(", declared_by=").append(a.getDeclaringType())
                    .append(", java_member=").append(a.getJavaMember())
                    .append(", java_type=").append(a.getJavaType())
                    .append('}');
        }
    }
    
    private String toString(Map map) {
        if(map == null || map.isEmpty()) {
            return "\n{}";
        }else{
            final StringBuilder b = new StringBuilder();
            map.forEach((k, v) -> {
                b.append('\n').append(k).append('=').append(v);
            });
            return b.toString();
        }
    }
}
