package com.looseboxes.webform.form;

import com.bc.webform.Form;
import com.bc.webform.FormMember;
import com.bc.webform.functions.TypeTests;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class JacksonFormMemberFilter extends JacksonDomainTypeFilter{
    
    private static final Logger LOG = LoggerFactory.getLogger(JacksonFormMemberFilter.class);
    
    public static final String FILTER_ID = "JacksonFormMemberFilter";
    
    public JacksonFormMemberFilter(TypeTests typeTests) { 
        super(typeTests);
    }

    @Override
    public boolean ignore(Class parentType, String name, Object value) {
        
        final boolean ignore = 
                super.ignore(parentType, name, value) ||
                (FormMember.class.isAssignableFrom(parentType) && 
                    (value instanceof Form || value instanceof Field)); 
        
        if(ignore) {
            LOG.debug("Ignore: {}, parent type: {}, {} = {}", ignore,
                    parentType.getSimpleName(), name, value);
        }else{
            LOG.trace("Ignore: {}, parent type: {}, {} = {}", ignore,
                    parentType.getSimpleName(), name, value);
        }

        return ignore;
    }
}
