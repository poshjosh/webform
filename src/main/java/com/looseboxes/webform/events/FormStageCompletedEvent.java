package com.looseboxes.webform.events;

import com.looseboxes.webform.web.FormConfigDTO;
import org.springframework.context.ApplicationEvent;

/**
 * @author hp
 */
public class FormStageCompletedEvent extends ApplicationEvent{
    
    private final FormConfigDTO formConfig;

    public FormStageCompletedEvent(Object source, FormConfigDTO formConfig) {
        super(source);
        this.formConfig = formConfig;
    }

    public FormConfigDTO getFormConfig() {
        return formConfig;
    }
}

