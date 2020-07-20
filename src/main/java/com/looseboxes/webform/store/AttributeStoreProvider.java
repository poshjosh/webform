/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.looseboxes.webform.store;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.ModelMap;

/**
 *
 * @author hp
 */
public interface AttributeStoreProvider {
    
    List<AttributeStore> all(StoreDelegate delegate);

    AttributeStore<ModelMap> forModel(ModelMap model);

    AttributeStore<HttpServletRequest> forRequest(HttpServletRequest request);

    AttributeStore<HttpSession> forSession(HttpSession session);
}
