package com.looseboxes.webform.form;

import com.bc.webform.form.member.FormMember;
import com.bc.webform.form.PreferMandatory;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comparator which considers the {@link com.looseboxes.webform.WebformProperties#ORDER ORDER}
 * property declared in the properties file. 
 * 
 * See the properties file for documentation.
 * 
 * @author hp
 */
public class FormMemberComparatorImpl 
        extends PreferMandatory<Field, Object> implements FormMemberComparator{

    private static final Logger LOG = LoggerFactory.getLogger(FormMemberComparatorImpl.class);
    
    private final PropertySearch propertySearch;

    public FormMemberComparatorImpl(PropertySearch propertySearch) {
        this.propertySearch = Objects.requireNonNull(propertySearch);
    }

    @Override
    public int compare(FormMember<Field, Object> lhs, FormMember<Field, Object> rhs) {
        
        int result = this.comparePriorities(lhs, rhs);
        
        if(result == 0) {
        
            result = super.compare(lhs, rhs);
        }
        
        return result;
    }
    
    /**
     * @param lhs
     * @param rhs
     * @return 
     */
    public int comparePriorities(FormMember lhs, FormMember rhs) {
    
        final int left = this.getPriority(lhs);
        final int right = this.getPriority(rhs);
        
        final int result;
        if(left == -1 || right == -1) {
            
            // Deliberately inverted lhs with rhs
            //
            result = Integer.compare(right, left);
            
        }else{
            result = Integer.compare(left, right);
        }
    
        LOG.trace("Compared priorities: {}, lhs: {}, rhs: {}", 
                result, lhs.getName(), rhs.getName());

        return result;
    }
    
    /**
     * A smaller number implies a higher priority
     * @param member
     * @return The priority of the {@link com.bc.webform.form.member.FormMember}
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
    private List<String> _cachedOrder;
    
    /**
     * A smaller number implies a higher priority
     * @param field
     * @return The priority of the field
     */
    public int getPriority(Field field) {
        
        final Class type = field.getDeclaringClass();
        
        final List<String> order;
        
        if(type.equals(_cachedType)) {
            order = _cachedOrder;
        }else{
            order = this.getOrder(type);
            _cachedOrder = order;
            _cachedType = type;
        }

        final int result = order == null || order.isEmpty() ? -1 :
                this.indexOf(order, field);
        
        LOG.trace("Priority: {}, field: {}.{}", 
                result, type.getSimpleName(), field.getName());
        
        return result;
    }
    
    public List<String> getOrder(Class type) {

        final List<String> order = this.propertySearch
                .findAll(WebformProperties.ORDER, type);

        if(LOG.isTraceEnabled()) {
            LOG.trace("Type: {}, Order: {}", type.getName(), order);
        }
        
        return order;
    }
    
    private int indexOf(List<String> order, Field field) {
        int index = -1;
        final String [] fieldNames = this.propertySearch.getFieldNames(field);
        for(String fieldName : fieldNames) {
            index = order.indexOf(fieldName);
            if(index != -1) {
                break;
            }
        }
        if(LOG.isTraceEnabled()) {
            LOG.trace("Index: {}, Order: {}, Field: {}", index, order, field);
        }
        return index;
    }
}
/**
 * 
 * 

    public int comparePrioritiesOnlyIfBothHavePriorityValue(
            FormMember lhs, FormMember rhs, int resultIfNone) {
    
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
 * 
 */