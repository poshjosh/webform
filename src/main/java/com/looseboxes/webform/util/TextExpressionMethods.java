package com.looseboxes.webform.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author hp
 */
public interface TextExpressionMethods{

    ////////////////////////////////////////////////////////////////////////////
    // Do not rename these methods. They are called from property files       //  
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * @return current date as a {@link java.util.Date} with time parts 
     * set to zero e.g yyyy-MM-dd
     */
    Date current_date();
    
    /**
     * @return the current {@link java.time.LocalDate}
     */
    LocalDate current_date_local();

    /**
     * @return current date and time as a {@link java.util.Date}
     */
    Date current_datetime();
    
    /**
     * @return the current {@link java.time.ZonedDateTime}
     */
    ZonedDateTime current_datetime_zoned();
    
    /**
     * @return the current {@link java.time.LocalDateTime}
     */
    LocalDateTime current_datetime_local();
    
    /**
     * @return the current {@link java.time.Instant}
     */
    Instant current_instant();
    
    /**
     * @return current time e.g HH:mm:ss as a {@link java.util.Date}
     */
    Date current_time();
    
    /**
     * @return the current {@link java.time.LocalTime}
     */
    LocalTime current_time_local();

    /**
     * @return current system unix time in milliseconds
     */
    long current_timestamp();
}
