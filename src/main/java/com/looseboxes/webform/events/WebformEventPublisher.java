package com.looseboxes.webform.events;

import com.looseboxes.webform.FormStage;
import com.looseboxes.webform.web.FormConfigDTO;
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
    
    public void publishFormStageBegunEvent(FormConfigDTO formConfig, FormStage stage) {
        formConfig.setFormStage(stage);
        this.applicationEventPublisher.publishEvent(new FormStageBegunEvent(this, formConfig));
    }

    public void publishFormStageCompletedEvent(FormConfigDTO formConfig) {
        this.applicationEventPublisher.publishEvent(new FormStageCompletedEvent(this, formConfig));
    }
}
