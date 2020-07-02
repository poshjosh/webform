package com.looseboxes.webform.util;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * Build a list of entities containing all the nested entities relating to a
 * root entity in the right order beginning with the most remote nested entity
 * and ending with the root entity.
 * 
 * For example given <code>Person.address.region</code>
 * 
 * Will build a list containing <code>[Region, Address, Person]</code> so that 
 * you can persist/merge the <code>Region</code>, then the <code>Address</code>, 
 * then the <code>Person</code>.
 * 
 * <b>Note.</b> By default properties with <code>null</code> values are ignored.
 * @author chinomso bassey ikwuagwu
 * @param <T> The property type e.g {@link java.lang.reflect.Field} or 
 * {@link java.beans.PropertyDescriptor} or whatever abstraction of a bean
 * property 
 */
public interface ObjectGraphAsListBuilder<T> {

    /**
     * @param object
     * @param test To test each property and their value
     * @return 
     */
    List build(Object object, BiPredicate<T, Object> test);
}
