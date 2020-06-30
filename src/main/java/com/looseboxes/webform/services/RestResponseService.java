package com.looseboxes.webform.services;

import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.entity.EntityRepositoryProvider;
import com.looseboxes.webform.web.FormConfig;
import java.net.URI;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * @author hp
 */
//@Service
public class RestResponseService extends AbstractResponseService<Object>{
    
    private final EntityRepositoryProvider entityRepositoryProvider;

    public RestResponseService(
            EntityRepositoryProvider entityRepositoryProvider, 
            MessageAttributesService messageAttributesService) {
        super(messageAttributesService);
        this.entityRepositoryProvider = Objects.requireNonNull(entityRepositoryProvider);
    }
    
    @Override
    public ResponseEntity<Object> respond(FormConfig formConfig) {
        
        final CRUDAction action = formConfig.getCrudAction();
        
        final ResponseEntity<Object> result;

        switch(action) {
            case create:
                result = ResponseEntity.created(buildURIForRead(formConfig))
                        .body(Objects.requireNonNull(formConfig.getModelobject()));
                break;
            case read:
            case update:
                result = ResponseEntity.ok(
                        Objects.requireNonNull(formConfig.getModelobject()));
                break;
            case delete:
                result = ResponseEntity.noContent().build();
                break;
            default:
                throw Errors.unexpected(action, (Object[])CRUDAction.values());
        }
        
        return result;
    }
    
    private URI buildURIForRead(FormConfig formConfig) {
        final Object modelobject = formConfig.getModelobject();
        final Object id = entityRepositoryProvider.forEntity(modelobject.getClass())
                .getIdOptional(modelobject).orElse(null);
        Objects.requireNonNull(id);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
          .path(this.buildPathForRead(formConfig))
          .buildAndExpand(id)
          .toUri();
        return uri;
    }
    
    private String buildPathForRead(FormConfig formConfig) {
        return "/" + CRUDAction.read + "/" + formConfig.getModelname() + "/{"+Params.MODELID+"}";
    }
}
