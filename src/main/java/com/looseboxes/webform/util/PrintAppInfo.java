package com.looseboxes.webform.util;

import com.bc.jpa.spring.PrintDatabaseInfo;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

/**
 * @author hp
 */
public class PrintAppInfo extends PrintDatabaseInfo implements CommandLineRunner{

    private static final Logger LOG = LoggerFactory.getLogger(PrintAppInfo.class);
    
    private final ApplicationContext context;
    
    public PrintAppInfo(ApplicationContext context) {
        super(context);
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public void run(String...args){

        System.out.println();
        LOG.debug("Printing command line arguments.");
        LOG.debug(args == null ? "null" : Arrays.toString(args));
        
        if( ! LOG.isDebugEnabled()) {
            return;
        }
        
        System.out.println();
        LOG.trace("Printing the beans provided by Spring Boot.");
        final String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        LOG.trace(Arrays.asList(beanNames).stream().collect(Collectors.joining("\n", "\n", "")));
        
        super.run(args);
    }
}
