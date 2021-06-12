package com.looseboxes.webform.events;

import com.looseboxes.webform.web.FormRequest;
import org.springframework.context.ApplicationEvent;

/**
 * @author hp
 */
public class FormStageCompletedEvent extends ApplicationEvent{
    
    private final FormRequest formRequest;

    public FormStageCompletedEvent(Object source, FormRequest formRequest) {
        super(source);
        this.formRequest = formRequest;
    }

    public FormRequest getFormRequest() {
        return formRequest;
    }
}

