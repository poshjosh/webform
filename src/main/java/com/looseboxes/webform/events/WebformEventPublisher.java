package com.looseboxes.webform.events;

import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.web.FormRequest;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author hp
 */
@Component
public class WebformEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;

    public WebformEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = 
                Objects.requireNonNull(applicationEventPublisher);
    }
    
    public void publishFormStageBegunEvent(FormRequest formRequest, FormStage stage) {
        formRequest.getFormConfig().setFormStage(stage);
        this.applicationEventPublisher.publishEvent(new FormStageBegunEvent(this, formRequest));
    }

    public void publishFormStageCompletedEvent(FormRequest formRequest) {
        this.applicationEventPublisher.publishEvent(new FormStageCompletedEvent(this, formRequest));
    }
}
