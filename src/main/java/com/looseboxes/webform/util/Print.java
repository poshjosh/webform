package com.looseboxes.webform.util;

import com.bc.webform.Form;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * @author hp
 */
public class Print<A extends Appendable> {
    
    private static final Logger LOG = LoggerFactory.getLogger(Print.class);
    
    private StringBuilder b = new StringBuilder();
    private String separator = "\n";
    
    public Print reset() {
        b.setLength(0);
        separator = "\n";
        return this;
    }
    
    public Print first(Object k, Object v) {
        this.reset();
        return this.add(k, v);
    }
    
    public Print add(Object k, Object v) {
        b.append(separator).append(k).append(" = ").append(v);
        return this;
    }
    
    public Print addHttpRequest(HttpServletRequest request) {
        b.append(separator).append(" - - - - - - - HttpServletRequest - - - - - - - ");
        this.add("request.characterEncoding", request.getCharacterEncoding());
        this.add("request.contentLength", request.getContentLengthLong());
        this.add("request.contentType", request.getContentType());
        this.add("request.header.rererer", request.getHeader("referer"));
        this.add("request.pathInfo", request.getPathInfo());
        this.add("request.queryString", request.getQueryString());
        this.add("request.requestUrI", request.getRequestURI());
        this.add("request.requestURL", request.getRequestURL());
        this.add("request.servletPath", request.getServletPath());
        final Enumeration<String> en1 = request.getParameterNames();
        while(en1.hasMoreElements()) {
            final String name = en1.nextElement();
            Object value = request.getParameter(name);
            if(value == null) {
                value = request.getParameterValues(name);
            }
            if(value instanceof Object[]) {
                value = Arrays.toString((Object[])value);
            }
            this.add(name, value);
        }
        final Enumeration<String> en2 = request.getAttributeNames();
        while(en2.hasMoreElements()) {
            final String name = en2.nextElement();
            // Contains much content
            if(name.startsWith("org.springframework.core.convert.ConversionService") || 
                    name.endsWith("converters")) {
                continue;
            }
            this.add(name, request.getAttribute(name));
        }
        return this;
    }

    public Print addHttpSession(HttpSession session) {
        b.append(separator).append(" - - - - - - - HttpSession - - - - - - - ");
        this.add("session.id", session.getId());
        final Enumeration<String> en = session.getAttributeNames();
        while(en.hasMoreElements()) {
            final String name = en.nextElement();
            this.add(name, session.getAttribute(name));
        }
        return this;
    }

    public Print traceAdded() {
        try{
            LOG.trace("{}", b.toString());
        }finally{
            this.reset();
        }
        return this;
    }
    
    public void trace(String prefix, Object obj, String separator, String suffix) {
        final String print = new Print<StringBuilder>().append(
                new StringBuilder(), obj, separator).toString();
        LOG.trace("{} {}", prefix, print, suffix);
    }

    public A append(A appendTo, Object obj, String separator) {
        final List<String> names = 
                Arrays.asList(obj.getClass().getDeclaredFields())
                .stream().map(field -> field.getName())
                .collect(Collectors.toList());
        return this.append(appendTo, obj, separator, names.toArray(new String[0]));
    }

    public A append(A appendTo, Object obj, String separator, String...names) {
        return this.append(appendTo, obj, separator, Arrays.asList(names));
    }
    
    public A append(A appendTo, Object obj, String separator, Collection<String> names) {
        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(obj);
        for(String name : names) {
            try{
                final Object val = bean.getPropertyValue(name);
                appendTo.append(separator).append(name).append('=').append(String.valueOf(val));
            }catch(Exception e) {
                System.err.println(separator + e.toString());
            }
        }
        return appendTo;
    }

    public void appendHtml(StringBuilder builder, Form<Object> form) {

        final String prefix = "<!DOCTYPE html>" +
        "<html>"+
                "<head><title>" + form.getLabel() + " Form</title></head>" +
                "<body>" + 
                    "<h3>Form display name: " + form.getLabel() + "</h3>";
        
        builder.append(prefix);
        
        final List<String> fieldNames = form.getMemberNames();
        
        for(String fieldName : fieldNames) {
            
            builder.append("<br/>");
            
            form.getMember(fieldName).ifPresent(ff -> {
            
                builder.append("<br/>").append("Form field name: ").append("<b>").append(fieldName).append("</b>");
                builder.append("<br/>").append("Advice").append(" = ").append(ff.getAdvice());
                builder.append("<br/>").append("Choices").append(" = ").append(ff.getChoices());
                builder.append("<br/>").append("Id").append(" = ").append(ff.getId());
                builder.append("<br/>").append("Label").append(" = ").append(ff.getLabel());
                builder.append("<br/>").append("Max length").append(" = ").append(ff.getMaxLength());
                builder.append("<br/>").append("Name").append(" = ").append(ff.getName());
                builder.append("<br/>").append("Number of lines").append(" = ").append(ff.getNumberOfLines());
                builder.append("<br/>").append("Reference form").append(" = ").append(ff.getReferencedForm());
                builder.append("<br/>").append("Size").append(" = ").append(ff.getSize());
                final String type = ff.getType();
                builder.append("<br/>").append("Type").append(" = ").append(type);
                builder.append("<br/>").append("Value").append(" = ").append(ff.getValue());
            });
        }
        
        final String suffix = "</body></html>";
        
        builder.append(suffix);
    }
}
