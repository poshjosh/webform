package com.looseboxes.webform.converters;

import com.looseboxes.webform.converters.DateToStringConverter;
import com.looseboxes.webform.StringUtils;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.TemporalType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author hp
 */
public class DateToStringConverterTest {
    
    private final SimpleDateFormat dateFormat = null;
    
    public DateToStringConverterTest() { }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of convert method, of class DateToStringConverter.
     */
    @Test
    public void testConvert() {
        System.out.println("convert");
        Date from = null;
        DateToStringConverter instance = null;
        String expResult = "";
//        String result = instance.convert(from);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
    public String convert(Date date, TemporalType tt) {
        
        String output = null;
        
        final String val = this.getPatterns(tt);
        
        if(val != null && !val.isEmpty()) {
            
            final String fmt = StringUtils
                    .toArrayStream(val).findFirst().orElse(null);
            
            if(fmt != null && !fmt.isEmpty()) {
                
                dateFormat.applyLocalizedPattern(fmt);
                final String dateStr = dateFormat.format(date);
                System.out.println(
                        MessageFormat.format(
                                "DATES Pattern: {0}, input: {1}, output: {2}", 
                                fmt, date, dateStr));
                output = dateStr;
            }else{
                output = null;
                System.err.println(MessageFormat.format(
                        "No date/time format found for: {0}", tt));
            }
        }else{
            date = null;
            System.err.println(MessageFormat.format(
                    "No date/time format found for: {0}", tt));
        }
        return output;
    }
    
    private String getPatterns(TemporalType tt) {
        return "";
    }
}
