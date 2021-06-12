package com.looseboxes.webform;

import java.util.Objects;

/**
 * @author hp
 */
public enum FormStage implements FormStages{
    
    BEGIN(FormStages.begin),
    
    VALIDATE(FormStages.validate),
    
    SUBMIT(FormStages.submit),
    
    DEPENDENTS(FormStages.dependents),
    
    VALIDATE_SINGLE(FormStages.validateSingle);

    private final String label;
    
    private FormStage(String label) {
        this.label = Objects.requireNonNull(label);
    }

    public static boolean isFirst(FormStage stage) {
        return stage == null ? false : stage.isFirst();
    }
    
    public boolean isFirst() {
        return this == BEGIN;
    }
    
    public static boolean isLast(FormStage stage) {
        return stage == null ? false : stage.isLast();
    }
    
    public boolean isLast() {
        return this == SUBMIT;
    }
    
    @Override
    public String toString() {
        return this.label;
    }
}
