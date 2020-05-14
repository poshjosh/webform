package com.looseboxes.webform.store;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.ModelMap;

/**
 * @author hp
 */
public final class StoreDelegate {

    private final ModelMap modelMap;

    private final HttpServletRequest request;

    public StoreDelegate(ModelMap modelMap, HttpServletRequest request) {
        this.modelMap = Objects.requireNonNull(modelMap);
        this.request = Objects.requireNonNull(request);
    }

    public ModelMap getModelMap() {
        return modelMap;
    }

    public HttpServletRequest getRequest() {
        return request;
    }
}
