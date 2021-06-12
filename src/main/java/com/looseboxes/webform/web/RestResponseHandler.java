package com.looseboxes.webform.web;

import com.looseboxes.webform.CRUDAction;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.Params;
import com.looseboxes.webform.repository.EntityRepositoryProvider;
import java.net.URI;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * @author hp
 */
public class RestResponseHandler implements ResponseHandler<FormConfigDTO, ResponseEntity<Object>>{
    
    private static final Logger LOG = LoggerFactory.getLogger(RestResponseHandler.class);
    
    private final EntityRepositoryProvider entityRepositoryProvider;

    public RestResponseHandler(EntityRepositoryProvider entityRepositoryProvider) {
        this.entityRepositoryProvider = Objects.requireNonNull(entityRepositoryProvider);
    }

    @Override
    public ResponseEntity<Object> respond(FormConfigDTO formConfig, Exception e) {
        LOG.warn("Unexpected exception", e);
        if(formConfig == null) {
            return ResponseEntity.badRequest().build();
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<Object> respond(FormConfigDTO formConfig) {
        
        final ResponseEntity<Object> result;
        if(formConfig == null) {
            result = ResponseEntity.badRequest().build();
        }else{
            if(FormStage.isLast(formConfig.getFormStage())) {
                formConfig.addInfo("Success");
            }
            final CRUDAction action = formConfig.getCrudAction();
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
                    throw Errors.unexpectedElement(action, CRUDAction.values());
            }
        }
        
        return result;
    }
    
    private URI buildURIForRead(FormConfig formConfig) {
        final Object modelobject = formConfig.getModelobject();
        final Object id = entityRepositoryProvider.getIdOptional(modelobject).orElse(null);
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
