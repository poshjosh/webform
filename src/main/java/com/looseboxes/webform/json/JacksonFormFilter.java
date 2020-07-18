package com.looseboxes.webform.json;

import com.bc.webform.form.Form;
import com.bc.webform.TypeTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class JacksonFormFilter extends JacksonDomainTypeRejectionFilter{
    
    private static final Logger LOG = LoggerFactory.getLogger(JacksonFormFilter.class);
    
    public static final String FILTER_ID = "JacksonFormFilter";
    
    public JacksonFormFilter(TypeTests typeTests) { 
        super(typeTests);
    }

    @Override
    public boolean ignore(Class parentType, String name, Object value) {
        
        final boolean ignore = 
//                super.isDomainType(parentType) ||
                (Form.class.isAssignableFrom(parentType) && 
                    (value != null && super.isDomainType(value.getClass()))); 
        
        LOG.trace("Ignore: {}, parent type: {}, {} = {}", ignore,
                parentType.getSimpleName(), name, value);

        return ignore;
    }
}
