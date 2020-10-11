package com.looseboxes.webform.form.util;

import com.bc.jpa.spring.TypeFromNameResolver;
import com.bc.webform.form.Form;
import com.looseboxes.webform.form.FormFactory;
import com.looseboxes.webform.domain.ObjectGraphBuilder;
import com.looseboxes.webform.web.FormRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class ModelObjectImagePathsProviderImpl implements ModelObjectImagePathsProvider{

    private final Logger log = LoggerFactory.getLogger(ModelObjectImagePathsProviderImpl.class);
    
    private final ObjectGraphBuilder objectGraphBuilder;
    private final TypeFromNameResolver typeFromNameResolver;
    private final FormFactory formFactory;

    public ModelObjectImagePathsProviderImpl(
            ObjectGraphBuilder objectGraphBuilder, 
            TypeFromNameResolver typeFromNameResolver, 
            FormFactory formFactory) {
        this.objectGraphBuilder = Objects.requireNonNull(objectGraphBuilder);
        this.typeFromNameResolver = Objects.requireNonNull(typeFromNameResolver);
        this.formFactory = Objects.requireNonNull(formFactory);
    }
    
    @Override
    public List<String> getImagePaths(FormRequest<Object> formRequest) {
        
        final List<String> result = new ArrayList<>();
        
        final Object entityOrDTO = formRequest.getFormConfig().getModelobject();
        
        // The image may be a property of a nested entity so we build the
        // entity graph and search for any image types
        //
        List<Object> entityList = this.objectGraphBuilder.build(entityOrDTO);
        
        for(Object entity : entityList) {
        
            final Class entityType = entity.getClass();
            
            final String typeId = this.typeFromNameResolver.getName(entityType);
            
            Form<Object> form = this.formFactory.newForm(
                    null, Long.toHexString(System.currentTimeMillis()), typeId, entity);
            
            result.addAll(this.getImagePaths(form));
        
        }
        
        log.debug("Entity: {}, image paths: {}", entityOrDTO, result);
        
        return result.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(result);
    }

    public List<String> getImagePaths(Form<Object> form) {
        
        final Object model = form.getDataSource();
        
        return form.getMembers().stream()
                .filter(member -> "file".equals(member.getType()))
                .map((member) -> {
                    final Object value = member.getValue();
                    return value == null ? (String)null : value.toString();
                })
                .filter(imagePath -> imagePath != null)
                .collect(Collectors.toList());
    }
}
