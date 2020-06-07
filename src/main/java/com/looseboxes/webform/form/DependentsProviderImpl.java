package com.looseboxes.webform.form;

import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.webform.functions.TypeTests;
import com.looseboxes.webform.WebformDefaults;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.util.PropertySearch;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * @author hp
 */
public class DependentsProviderImpl implements DependentsProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(DependentsProviderImpl.class);
    
    private final PropertySearch propertySearch;
    private final EntityRepositoryFactory repoFactory;
    private final TypeTests typeTests;

    public DependentsProviderImpl(
            PropertySearch propertySearch, 
            EntityRepositoryFactory repoFactory, 
            TypeTests typeTests) {
        this.propertySearch = Objects.requireNonNull(propertySearch);
        this.repoFactory = Objects.requireNonNull(repoFactory);
        this.typeTests = Objects.requireNonNull(typeTests);
    }

    /**
     * Provide lists of dependents based on current selection.
     * 
     * For example if an Address Form has both country and region inputs, and
     * the region input is dependent on the country input. Then, when the 
     * country input is selected, use this method to return the list of regions
     * (or other dependent input) for the selected country. The regions returned 
     * should immediately be rendered in the form giving the user an option to 
     * select from.
     * 
     * Given the following domain definition:
     * 
     * <code>
     * <pre>
     * class Address{
     *      Long id;
     *      Country country;
     *      Region region;
     *      String city;
     *      String streetAddress;
     * }
     * 
     * enum Country{
     *     NIGERIA, GERMANY, UNITED_STATES_OF_AMERICA;
     * }
     * 
     * class Region{
     *      Long id;
     *      Country country;
     *      String name;
     * }
     * </pre>
     * </code>
     * 
     * We want the form to automatically populate our region fields when
     * the country field is selected.
     * <p>
     * For this method, the Address model object will be the first argument and
     * the name of the country field of (e.g "country") will be the second 
     * argument.
     * </p>
     * The return value will include an entry with key equal to "region" and 
     * value equal to the list of regions for the selected country.
     * <p>
     * A value for country (representing the selected country) must have been 
     * set in the Address model object, or this method returns an empty list
     * for region.
     * </p>
     * @param modelobject
     * @param propertyName
     * @return 
     */
    @Override
    public Map<Class, List> getDependents(
            Object modelobject, String propertyName) {
        
        Objects.requireNonNull(modelobject);
        Objects.requireNonNull(propertyName);
        
        Map<Class, List> result = null;
        
        final BeanWrapper beanWrapper = PropertyAccessorFactory
                .forBeanPropertyAccess(modelobject);
        
        final Set<Class> dependentTypes = this.getDependentTypes(beanWrapper, propertyName);
        
        final Object propertyValue = beanWrapper.getPropertyValue(propertyName);

        final int limit = this.getMaxItemsInMultiChoice();
        
        for(Class dependentType : dependentTypes) {
            
            final EntityRepository repo = this.repoFactory.forEntity(dependentType);
        
            final List dependentEntities = repo.findAllBy(
                    propertyName, propertyValue, 0, limit);
            
            LOG.trace("Type: {}, values: {}", dependentType.getName(), dependentEntities);
            
            if(dependentEntities.isEmpty()) {
                continue;
            }
            
            if(result == null) {
                result = new HashMap(dependentTypes.size(), 1.0f);
            }
            
            result.put(dependentType, dependentEntities);
        }
        
        return this.ensureUnmodifiableMap(result);
    }
    
    public Set<Class> getDependentTypes(BeanWrapper beanWrapper, String propertyName) {
        
        final PropertyDescriptor [] pds = beanWrapper.getPropertyDescriptors();
        
        final Class propertyType = beanWrapper.getPropertyType(propertyName);
        
        Set<Class> result = null;
        
        for(PropertyDescriptor pd : pds) {
        
            final Class type = pd.getPropertyType();
            
            if(propertyName.equalsIgnoreCase(pd.getName())) {
                continue;
            }
            
            if( ! typeTests.isDomainType(type) || typeTests.isEnumType(type)) {
                continue;
            }
            
            if(this.hasFieldOfType(type, propertyType)) {
            
                if(result == null) {
                
                    result = new HashSet();
                }
                
                result.add(type);
            }
        }
        
        LOG.debug("{}#{} has dependent types: {}", 
                beanWrapper.getWrappedClass().getName(), propertyName, result);
        
        return result == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(result);
    }
    
    public boolean hasFieldOfType(Class objectType, Class fieldType) {
        boolean result = false;
        final Field [] fields = objectType.getDeclaredFields();
        for(Field field : fields) {
            if(field.getType().equals(fieldType)) {
                result = true;
                break;
            }
        }
        LOG.trace("Has field of type: {}, object type: {}, field type: {}",
                result, objectType.getName(), fieldType.getName());
        return result;
    }
    
    public Map ensureUnmodifiableMap(Map map) {
        return map == null || map.isEmpty() ? Collections.EMPTY_MAP :
                Collections.unmodifiableMap(map);
    }

    public int getMaxItemsInMultiChoice() {
        final String maxItemsInMultiChoice = this.propertySearch.find(
                WebformProperties.MAX_ITEMS_IN_MULTICHOICE).orElse(null);
        return maxItemsInMultiChoice == null ? 
                WebformDefaults.MAX_ITEMS_IN_MULTICHOICE : 
                Integer.parseInt(maxItemsInMultiChoice);
    }
}
