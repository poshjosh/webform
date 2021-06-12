package com.looseboxes.webform.json;

import com.bc.webform.form.Form;
import com.bc.webform.form.member.FormMember;
import com.bc.webform.TypeTests;
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
                this.isNameToReject(name) ||
                this.isFormMemberDomainType(parentType, name, value); 
        
        LOG.trace("Ignore: {}, parent type: {}, {} = {}", ignore,
                parentType.getSimpleName(), name, value);

        return ignore;
    }

    private boolean isFormMemberDomainType(Class parentType, String name, Object value) {
        return (FormMember.class.isAssignableFrom(parentType) && 
                    (value instanceof Form || value instanceof Field));
    }
}
