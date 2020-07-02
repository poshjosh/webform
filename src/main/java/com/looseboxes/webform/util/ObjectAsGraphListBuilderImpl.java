package com.looseboxes.webform.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ObjectAsGraphListBuilderImpl implements ObjectGraphAsListBuilder<Field> {
    
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
    public List build(Object object, BiPredicate<Field, Object> test) {
        final List list = this.buildChildren(object, test);
        final List result;
        if(list.isEmpty()) {
            result = Collections.singletonList(object);
        }else{
            list.add(object);
            result = Collections.unmodifiableList(list);
        }
        if(LOG.isDebugEnabled()) {
            LOG.debug("Object graph as list: {}", 
                    result.stream().map(Object::toString)
                            .collect(Collectors.joining("\n", "[\n", "\n]")));
        }
        return result;
    }
    
    public List buildChildren(Object object, BiPredicate<Field, Object> test) {
        List<Node> collectInto = new ArrayList<>();
        this.build(object.getClass(), object, test, collectInto, 0);
        LOG.trace("Before sort: {}", collectInto);
        Collections.sort(collectInto, Collections.reverseOrder());
        LOG.trace(" After sort: {}", collectInto);
        List result = collectInto.stream()
                .map((node) -> node.value).collect(Collectors.toList());
        return result;
    }

    private Field [] build(Class beanType, Object bean, 
            BiPredicate<Field, Object> propertyTest, List<Node> collectInto, int depth) {
        
        LOG.trace("Depth: {}, bean: {}, collected: {}", 
                depth, beanType.getName(), collectInto.size());
        
        if(this.isMaxDepthReached(depth)) {
            LOG.trace("Max depth exceeded. Depth: {}, max depth: {}", depth, maxDepth);
            return new Field[0];
        }
    
        final Field [] fields = this.getFields(beanType);
        
        for(Field field : fields){
            
            final Class fieldType = field.getType();
            
            final Object fieldValue = FieldUtils.getFieldValue(bean, field);
            
            final boolean accept = fieldValue != null && propertyTest.test(field, fieldValue);
            
            LOG.trace("Accept: {}, {}#{} = {}", accept, beanType.getName(), field.getName(), fieldValue);
            
            if(accept) {
                
                this.build(fieldType, fieldValue, propertyTest, collectInto, depth + 1);
                
                Node node = new Node();
                node.depth = depth;
                node.value = fieldValue;
                collectInto.add(node);
                LOG.trace("Added: {}", node);
            }
        }
        
        return fields;
    }
    
    private boolean isMaxDepthReached(int depth) {
        return this.isMaxDepthEnabled() && depth >= maxDepth;
    }
    
    private boolean isMaxDepthEnabled() {
        return this.maxDepth > -1;
    }
    
    private Field [] getFields(Class entityType) {
        final Field [] fields = entityType.getDeclaredFields();
        return fields;
    }
}
