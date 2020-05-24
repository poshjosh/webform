package com.looseboxes.webform.form;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

/**
 * @author hp
 */
public class PropertyExpressionsResolverImpl implements PropertyExpressionsResolver {
    
//    private static final Logger LOG = LoggerFactory.getLogger(PropertyExpressionsResolverImpl.class);
    
    public PropertyExpressionsResolverImpl() { }
    
    /**
     * @return current date with time parts set to zero e.g yyyy-MM-dd
     */
    @Override
    public Object current_date() {
        final Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        final Date result = cal.getTime();
        return result;
    }
    
    /**
     * @return current date and time e.g yyyy-MM-dd'T'HH:mm:ss
     */
    @Override
    public Object current_datetime() {
        return new Date();
    }
    
    /**
     * @return current time e.g HH:mm:ss
     */
    @Override
    public Object current_time() {
        return LocalTime.now();
    }

    /**
     * @return current system unix time in milliseconds
     */
    @Override
    public Object current_timestamp() {
        return System.currentTimeMillis();
    }
}
