package com.looseboxes.webform.events;

import com.looseboxes.webform.web.FormRequest;
import org.springframework.context.ApplicationEvent;

/**
 * @author hp
 */
public class FormStageBegunEvent extends ApplicationEvent{
    
    private final FormRequest formRequest;

    public FormStageBegunEvent(Object source, FormRequest formRequest) {
        super(source);
        this.formRequest = formRequest;
    }

    public FormRequest getFormRequest() {
        return formRequest;
    }
}
