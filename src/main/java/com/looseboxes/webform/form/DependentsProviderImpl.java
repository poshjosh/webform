package com.looseboxes.webform.form;

import com.looseboxes.webform.repository.EntityRepository;
import com.bc.webform.TypeTests;
import com.looseboxes.webform.WebformDefaults;
import com.looseboxes.webform.WebformProperties;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import com.looseboxes.webform.converters.DomainTypeConverter;
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
import org.springframework.core.convert.TypeDescriptor;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import java.util.Locale;

/**
 * @author hp
 */
public class DependentsProviderImpl implements DependentsProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(DependentsProviderImpl.class);
    
    private final PropertySearch propertySearch;
    private final EntityRepositoryProvider entityRepositoryProvider;
    private final TypeTests typeTests;
    private final DomainTypeConverter domainTypeConverter;
    private final DomainObjectPrinter domainObjectPrinter;

    public DependentsProviderImpl(
            PropertySearch propertySearch, 
            EntityRepositoryProvider entityRepositoryProvider, 
            TypeTests typeTests,
            DomainTypeConverter domainTypeConverter,
            DomainObjectPrinter domainObjectPrinter) {
        this.propertySearch = Objects.requireNonNull(propertySearch);
        this.entityRepositoryProvider = Objects.requireNonNull(entityRepositoryProvider);
        this.typeTests = Objects.requireNonNull(typeTests);
        this.domainTypeConverter = Objects.requireNonNull(domainTypeConverter);
        this.domainObjectPrinter = Objects.requireNonNull(domainObjectPrinter);
    }

    /**
     * @see #getDependents(java.lang.Object, java.lang.String, java.lang.String) 
     * @param modelobject The model object whose field dependents are required
     * @param propertyName The name of the field/property for which dependents are required
     * @param propertyValue The value of the field/property for which dependents are required
     * @param locale The locale to use in printing the dependents
     * @return The dependents for the field/property of the model object
     */
    @Override
    public Map<String, Map> getChoicesForDependents(
            Object modelobject, String propertyName, 
            String propertyValue, Locale locale) {
    
        final Map<PropertyDescriptor, List> dependents = this.getDependents(
                modelobject, propertyName, propertyValue);
        
        final Map<String, Map> result;
        
        if(dependents == null || dependents.isEmpty()) {
            
            result = Collections.EMPTY_MAP;
            
        }else{
            
            result = new HashMap(dependents.size(), 1.0f);

            dependents.forEach((propertyDescriptor, entityList) -> {
                final Map choices = new HashMap(entityList.size(), 1.0f);
                final Class entityType = propertyDescriptor.getPropertyType();
                final EntityRepository repo = entityRepositoryProvider.forEntity(entityType);
                for(Object entity : entityList) {
                    final Object key = repo.getIdOptional(entity).orElse(null);
                    Objects.requireNonNull(key);
                    final Object val = domainObjectPrinter.print(entity, locale);
                    Objects.requireNonNull(val);
                    choices.put(key, val);
                }
                final String name = propertyDescriptor.getName();
                result.put(name, choices);
            });
        }
        
        return result;
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
     * The returned value will include an entry with key equal to a 
     * PropertyDescriptor (whose name is "region", and propertyType is Region.class) 
     * and value equal to the list of regions for the selected country.
     * <p>
     * <b>Note:</b>
     * </p>
     * A value for country (representing the selected country) must have been 
     * set in the Address model object, or this method returns an empty list
     * for region.
     * @param modelobject The model object whose field dependents are required
     * @param propertyName The name of the field/property for which dependents are required
     * @param propertyValue The value of the field/property for which dependents are required
     * @return The dependents for the field/property of the model object
     */
    @Override
    public Map<PropertyDescriptor, List> getDependents(
            Object modelobject, String propertyName, String propertyValue) {
        
        Objects.requireNonNull(modelobject);
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(propertyValue);
        
        LOG.debug("Property: {} = {}, modelobject: {}", propertyName, propertyValue, modelobject);
        
        Map<PropertyDescriptor, List> result = null;
        
        final BeanWrapper beanWrapper = PropertyAccessorFactory
                .forBeanPropertyAccess(modelobject);
        
        final PropertyDescriptor [] beanProperties = beanWrapper.getPropertyDescriptors();
        
        final TypeDescriptor propertyType = beanWrapper.getPropertyTypeDescriptor(propertyName);
        
        final Set<PropertyDescriptor> dependentTypes = this.getDependentProperties(
                modelobject.getClass(), beanProperties, propertyType, propertyName);
        
        if( ! dependentTypes.isEmpty()) {
        
            final int limit = this.getMaxItemsInMultiChoice();

            final String name = propertyName;
            final Object value = this.domainTypeConverter.convert(
                    propertyValue, TypeDescriptor.valueOf(String.class), propertyType);

            final int offset = 0;

            for(PropertyDescriptor dependentProperty : dependentTypes) {

                final Class dependentType = dependentProperty.getPropertyType();

                final EntityRepository repo = this.entityRepositoryProvider.forEntity(dependentType);

                LOG.debug("SELECT ALL FROM {} WHERE {} = {}, RETURN RECORDS {} - {}", 
                        dependentType, name, value, offset, limit);

                final List dependentEntities = repo.findAllBy(
                        name, value, offset, limit);

                LOG.trace("Type: {}, values: {}", dependentType.getName(), dependentEntities);

                if(dependentEntities.isEmpty()) {
                    continue;
                }

                if(result == null) {
                    result = new HashMap(dependentTypes.size(), 1.0f);
                }

                result.put(dependentProperty, dependentEntities);
            }
        }
        
        return this.ensureUnmodifiableMap(result);
    }
    
    public Set<PropertyDescriptor> getDependentProperties(
            Class beanType, PropertyDescriptor [] beanProperties, 
            TypeDescriptor selectedType, String selectedName) {
        
        final Class propertyType = selectedType == null ? null : selectedType.getType();
        
        LOG.debug("Property name: {}, type: {}, type descriptor: {}", 
                selectedName, propertyType, selectedType);

        Set<PropertyDescriptor> result = null;
        
        if(propertyType != null) {
        
            for(PropertyDescriptor beanProperty : beanProperties) {

                final String name = beanProperty.getName();
                final Class type = beanProperty.getPropertyType();

                LOG.trace("Checking name: {}, type: {}", name, type);

                if(propertyType == null && type == null) {
                    LOG.trace("Rejected name: {}, both types are null", name);
                    continue;
                }

                final boolean rejectedSelf = (selectedName.equalsIgnoreCase(name) ||
                        Objects.equals(propertyType, type));

                LOG.trace("Rejected self: {}, name: {}, type: {}", rejectedSelf, name, type);

                if(rejectedSelf) {
                    continue;
                }

                final boolean rejected = ( ! typeTests.isDomainType(type) || typeTests.isEnumType(type));

                LOG.trace("Rejected: {}, name: {}, type: {}", rejected, name, type);

                if(rejected) {
                    continue;
                }

                if(this.hasFieldOfType(type, propertyType)) {

                    if(result == null) {

                        result = new HashSet();
                    }

                    result.add(beanProperty);
                }
            }
        }
        
        LOG.debug("{}#{} has dependent types: {}", 
                beanType.getName(), selectedName, result);
        
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
        LOG.debug("Has field of type: {}, object type: {}, field type: {}",
                result, objectType, fieldType);
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
