package com.looseboxes.webform.controllers;

import com.looseboxes.webform.services.ModelObjectService;
import java.util.Enumeration;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public final class Print {
    
    private static final Logger LOG = LoggerFactory.getLogger(Print.class);

    public static void formAttributes(String tag, HttpSession session) {
        Enumeration<String> en = session.getAttributeNames();
        StringBuilder buffer = new StringBuilder("\n->");
        buffer.append(tag).append(" Printing form attribute(s) in session: ");
        buffer.append(session.getId());
        while(en.hasMoreElements()) {
            String name = en.nextElement();
            if(name.startsWith(ModelObjectService.FORM_ID_PREFIX)) {
                buffer.append("\n->").append(session.getAttribute(name));
            }
        }
        LOG.debug("{}", buffer);
    }
}
