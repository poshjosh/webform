package com.looseboxes.webform.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build a list of entities containing all the nested entities relating to an 
 * entity in the right order beginning with the most remote nested entity
 * and ending with the root object for which nested entities are being collected.
 * 
 * 
 * For example given <code>Person.address.region</code>
 * 
 * Will build a list containing <code>[Region, Address]</code> so that you can 
 * persist the <code>Region</code>, then the <code>Address</code>, then the Person.
 * 
 * <b>Note.</b> By default properties with <code>null</code> values are ignored.
 * @author chinomso bassey ikwuagwu
 */
public class ObjectAsGraphListBuilderImpl implements ObjectGraphAsListBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAsGraphListBuilderImpl.class);
    
    private static class Node implements Comparable{

        private int depth;
        private Object value;
        
        @Override
        public int compareTo(Object o) {
            return Integer.compare(depth, ((Node)o).depth);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Node other = (Node) obj;
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Node{" + "depth=" + depth + ", value=" + value + '}';
        }
    }
    
    private final int maxDepth;
    
    public ObjectAsGraphListBuilderImpl(int maxDepth) { 
        if(maxDepth < -1) {
            throw new IllegalArgumentException();
        }
        this.maxDepth = maxDepth;
    }
    
    @Override
    public List build(Object object, Predicate test) {
        List result = this.buildChildren(object, test);
        result.add(object);
        return Collections.unmodifiableList(result);
    }
    
    public List buildChildren(Object object, Predicate test) {
        List<Node> collectInto = new ArrayList<>();
        this.build(object.getClass(), object, test, collectInto, 0);
        LOG.trace("Before sort: {}", collectInto);
        Collections.sort(collectInto, Collections.reverseOrder());
        LOG.trace(" After sort: {}", collectInto);
        List result = collectInto.stream()
                .map((node) -> node.value).collect(Collectors.toList());
        LOG.debug("Entity: {}, object graph as list: {}", 
                object.getClass().getSimpleName(), 
                result.stream().map((e) -> e.getClass().getSimpleName())
                        .collect(Collectors.toList()));
        return result;
    }

    private void build(Class beanType, Object bean, 
            Predicate propertyTest, List<Node> collectInto, int depth) {
        
        LOG.trace("Depth: {}, bean: {}, collected: {}", 
                depth, bean.getClass().getName(), collectInto.size());
        
        if(this.isMaxDepthReached(depth)) {
            LOG.trace("Max depth exceeded. Depth: {}, max depth: {}", depth, maxDepth);
            return;
        }
    
        final Field [] fields = this.getFields(beanType);
        
        for(Field field : fields){
            
            LOG.trace("Processing field: {}#{} of type {}", 
                    bean.getClass().getSimpleName(), field.getName(), field.getType());
            
            if(propertyTest.test(field)) {
                
                final Class fieldType = field.getType();
                final Object fieldValue = this.getFieldValue(bean, field);
                
                if(fieldValue == null) {
                    continue;
                }
                
                this.build(fieldType, fieldValue, propertyTest, collectInto, depth + 1);
                
                Node node = new Node();
                node.depth = depth;
                node.value = fieldValue;
                collectInto.add(node);
                LOG.trace("Added: {}", node);
            }
        }
        
    }
    
    private boolean isMaxDepthReached(int depth) {
        return this.isMaxDepthEnabled() && depth >= maxDepth;
    }
    
    private boolean isMaxDepthEnabled() {
        return this.maxDepth > -1;
    }
    
    private Object getFieldValue(Object source, Field field) {
        final boolean accessible = field.isAccessible();
        try{
            if( ! accessible) {
                field.setAccessible(true);
            }
            return field.get(source);
        }catch(IllegalAccessException | IllegalArgumentException | SecurityException e) {
            LOG.warn("Failed to access value of field: " + field + " on instance: " + source, e);
            throw new RuntimeException(e);
        }finally{
            if( ! accessible) {
                field.setAccessible(accessible);
            }
        }
    }
    
    private Field [] getFields(Class entityType) {
        final Field [] fields = entityType.getDeclaredFields();
        return fields;
    }
}
