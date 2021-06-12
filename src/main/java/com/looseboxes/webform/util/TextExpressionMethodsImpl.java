package com.looseboxes.webform.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

/**
 * @author hp
 */
public class TextExpressionMethodsImpl implements TextExpressionMethods {
    
//    private static final Logger LOG = LoggerFactory.getLogger(TextExpressionMethodsImpl.class);
    
    public TextExpressionMethodsImpl() { }
    
    ////////////////////////////////////////////////////////////////////////////
    // Do not rename these methods. They are called from property files       //  
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * @return current date as a {@link java.util.Date} with time parts 
     * set to zero e.g yyyy-MM-dd
     */
    @Override
    public Date current_date() {
        final Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        final Date result = cal.getTime();
        return result;
    }
    
    /**
     * @return the current {@link java.time.LocalDate}
     */
    @Override
    public LocalDate current_date_local() {
        return LocalDate.now();
    }

    /**
     * @return current date and time as a {@link java.util.Date}
     */
    @Override
    public Date current_datetime() {
        return new Date();
    }
    
    /**
     * @return the current {@link java.time.ZonedDateTime}
     */
    @Override
    public ZonedDateTime current_datetime_zoned() {
        return ZonedDateTime.now();
    }
    
    /**
     * @return the current {@link java.time.LocalDateTime}
     */
    @Override
    public LocalDateTime current_datetime_local() {
        return LocalDateTime.now();
    }
    
    /**
     * @return the current {@link java.time.Instant}
     */
    @Override
    public Instant current_instant() {
        return Instant.now();
    }
    
    /**
     * @return current time e.g HH:mm:ss as a {@link java.util.Date}
     */
    @Override
    public Date current_time() {
        final Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        final Date result = cal.getTime();
        return result;
    }
    
    /**
     * @return the current {@link java.time.LocalTime}
     */
    @Override
    public LocalTime current_time_local() {
        return LocalTime.now();
    }

    /**
     * @return current system unix time in milliseconds
     */
    @Override
    public long current_timestamp() {
        return System.currentTimeMillis();
    }
}
