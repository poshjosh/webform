package com.looseboxes.webform.json;

import com.bc.webform.Form;
import com.bc.webform.FormMember;
import com.bc.webform.functions.TypeTests;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class JacksonFormMemberFilter extends JacksonDomainTypeRejectionFilter{
    
    private static final Logger LOG = LoggerFactory.getLogger(JacksonFormMemberFilter.class);
    
    public static final String FILTER_ID = "JacksonFormMemberFilter";
    
    public JacksonFormMemberFilter(TypeTests typeTests) { 
        super(typeTests);
    }

    @Override
    public boolean ignore(Class parentType, String name, Object value) {
        
        final boolean ignore = 
//                super.isDomainType(parentType) ||
                (FormMember.class.isAssignableFrom(parentType) && 
                    (value instanceof Form || value instanceof Field)); 
        
        LOG.trace("Ignore: {}, parent type: {}, {} = {}", ignore,
                parentType.getSimpleName(), name, value);

        return ignore;
    }
}
