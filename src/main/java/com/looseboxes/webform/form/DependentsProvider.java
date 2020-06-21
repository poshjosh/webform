package com.looseboxes.webform.form;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;

/**
 * @author hp
 */
public interface DependentsProvider {

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
     * @param modelobject
     * @param propertyName
     * @param propertyValue
     * @return
     */
    Map<PropertyDescriptor, List> getDependents(
            Object modelobject, String propertyName, String propertyValue);
}
