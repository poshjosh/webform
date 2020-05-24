package com.looseboxes.webform.form;

import com.bc.webform.FormMember;
import com.bc.webform.PreferMandatory;
import com.looseboxes.webform.util.StringArrayUtils;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.util.Arrays;
import javax.persistence.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class FormMemberComparatorImpl 
        extends PreferMandatory implements FormMemberComparator{

    private static final Logger LOG = LoggerFactory.getLogger(FormMemberComparatorImpl.class);
    
    private final PropertySearch propertySearch;

    public FormMemberComparatorImpl(PropertySearch propertySearch) {
        this.propertySearch = propertySearch.appendingInstance();
    }

    @Override
    public int compare(FormMember lhs, FormMember rhs) {
        
        int result = this.comparePriorities(lhs, rhs, 0);
        
        if(result == 0) {
        
            result = super.compare(lhs, rhs);
        }
        
        return result;
    }
    
    /**
     * lhs and rhs are only compared if they both have priorities
     * @param lhs
     * @param rhs
     * @param resultIfNone
     * @return 
     */
    public int comparePriorities(FormMember lhs, FormMember rhs, int resultIfNone) {
    
        final int left = this.getPriority(lhs);
        
        int result = resultIfNone;
        
        if(left != -1) {
            
            final int right = this.getPriority(rhs);
            
            if(right != -1) {
            
                result = Integer.compare(left, right);
            }
        }
    
        if(result == resultIfNone) {
            LOG.trace("Compared priorities: {}, lhs: {}, rhs: {}", result, lhs.getName(), rhs.getName());
        }else{
            LOG.debug("Compared priorities: {}, lhs: {}, rhs: {}", result, lhs.getName(), rhs.getName());
        }
        return result;
    }
    
    /**
     * A larger number implies a higher priority
     * @param member
     * @return The priority of the {@link com.bc.webform.FormMember}
     */
    public int getPriority(FormMember member) {
       
        final int index;
        
        final Object dataSource = member.getDataSource();
        
        if(dataSource instanceof Field) {
            
            index = this.getPriority((Field)dataSource);
            
        }else{
        
            index = -1;
        }
        
        return index;
    }
    
    private Class _cachedType;
    private String[] _cachedArr;
    
    /**
     * A larger number implies a higher priority
     * @param field
     * @return The priority of the field
     */
    public int getPriority(Field field) {
        
        final Class type = field.getDeclaringClass();
        
        final String [] order;
        
        if(type.equals(_cachedType)) {
            order = _cachedArr;
        }else{
            order = this.getOrder(type);
            _cachedArr = order;
            _cachedType = type;
        }
        
        final int n = order == null || order.length == 0 ? -1 :
                this.indexOf(order, field);
        
        final int result = n == -1 ? -1 : (order.length - 1) - n;
        
        LOG.trace("Priority: {}, field: {}.{}", 
                result, type.getSimpleName(), field.getName());
        
        return result;
    }
    
    public String [] getOrder(Class type) {
    
        final String [] order;
        
        final String value = this.propertySearch.findOrDefault(
                WebformProperties.ORDER, type, null);

        if(value == null || value.isEmpty()) {

            order = null;

        }else{

            order = StringArrayUtils.toArray(value);
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("Type: {}, Order: {}", type.getName(), 
                    (order == null ? null : Arrays.toString(order)));
        }
        
        return order;
    }
    
    private int indexOf(String[] order, Field field) {
        int n = this.indexOf(order, field.getName());
        if(n == -1) {
            final Column column = field.getAnnotation(Column.class);
            final String name = column == null ? null : column.name();
            n = this.indexOf(order, name);
        }
        if(LOG.isTraceEnabled()) {
            LOG.trace("Index: {}, Order: {}, Field: {}",
                    n, (order==null?null:Arrays.toString(order)), field);
        }
        return n;
    }
    
    private int indexOf(String [] order, String name) {
        for(int i=0; i<order.length; i++) {
            if(order[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
