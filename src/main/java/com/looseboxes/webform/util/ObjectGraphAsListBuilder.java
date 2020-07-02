package com.looseboxes.webform.util;

import java.util.List;
import java.util.function.Predicate;

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
 */
public interface ObjectGraphAsListBuilder {

    List build(Object object, Predicate test);
}
