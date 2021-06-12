package com.looseboxes.webform.store;

import com.looseboxes.webform.Wrapper;

/**
 * A {@link com.looseboxes.webform.store.Store Store} backed by another.
 * The backing instance could be, for example, a 
 * {@link org.springframework.ui.ModelMap ModelMap},
 * {@link javax.servlet.http.HttpServletRequest HttpServletRequest},
 * {@link javax.servlet.http.HttpSession HttpSession} or any other object.
 * @author hp
 */
public interface AttributeStore<S> 
        extends Store<String, Object>, Wrapper<S, AttributeStore> {

}
