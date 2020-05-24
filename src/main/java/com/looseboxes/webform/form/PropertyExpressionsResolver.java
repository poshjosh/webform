package com.looseboxes.webform.form;

/**
 * @author hp
 */
public interface PropertyExpressionsResolver{

    /**
     * @return current date with time parts set to zero e.g yyyy-MM-dd
     */
    Object current_date();

    /**
     * @return current date and time e.g yyyy-MM-dd'T'HH:mm:ss
     */
    Object current_datetime();

    /**
     * @return current time e.g HH:mm:ss
     */
    Object current_time();

    /**
     * @return current system unix time in milliseconds
     */
    Object current_timestamp();
}
