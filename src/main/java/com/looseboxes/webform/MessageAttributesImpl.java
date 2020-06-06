package com.looseboxes.webform;

/**
 * @author hp
 */
public class MessageAttributesImpl implements MessageAttributes{

    @Override
    public String getErrorMessages() {
        return "webform.messages.errors";
    }

    @Override
    public String getInfoMessages() {
        return "webform.messages.infos";
    }
}
