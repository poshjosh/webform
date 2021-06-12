package com.looseboxes.webform;

import java.util.Locale;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * @author hp
 */
@FunctionalInterface
public interface WebformLocaleSupplier {
    
    Locale DEFAULT_LOCALE = WebformDefaults.LOCALE;
    
    Locale getLocale();
    
    public static Locale getLocale(ApplicationContext context) {
        try{
            return context.getBean(WebformLocaleSupplier.class).getLocale();
        }catch(NoSuchBeanDefinitionException ignored) {
            return DEFAULT_LOCALE;
        }
    }
}
