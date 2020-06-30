package com.looseboxes.webform.util;

import com.looseboxes.webform.TestConfig;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author hp
 */
public class PropertySearchTest {
    
    private static final String PREFIX = "g2_mac-33";
    private static final String DEFAULT_PROPERTY_NAME = "dateTimeFormat";
    
    private static final class Person{
        private String name;
        private Date dateOfBirth;
    }
    
    private Properties properties = new Properties();
    
    public PropertySearchTest() { }
    
    @BeforeEach
    public void setUp() {
        properties = new Properties();
    }

    /**
     * Test of find method, of class PropertySearch.
     * @throws java.lang.NoSuchFieldException
     */
    @Test
    public void testGetProperty_Field_String() throws NoSuchFieldException{
        System.out.println("getProperty");
        final Field field = Person.class.getDeclaredField("dateOfBirth");
        final String fieldName = field.getName();
        final String propertyName = this.getDefaultPropertyName();
        final String key = propertyName + '.' + Person.class.getName() + '.' + fieldName;
        final String value = this.getPropertyValue(fieldName, propertyName);
        this.setPrefixedProperty(key, value);
        final PropertySearch instance = this.getInstance();
        final Optional<String> expResult = Optional.ofNullable(this.getPrefixedProperty(key, null));
        final Optional<String> result = instance.find(propertyName, field);
        System.out.println("Expect: " + expResult);
        System.out.println(" Found: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class PropertySearch.
     */
    @Test
    public void testGetProperty_Class_String() {
        System.out.println("getProperty");
        final Class type = Person.class;
        final String propertyName = this.getDefaultPropertyName();
        final String key = propertyName + '.' + Person.class.getName();
        final String value = this.getPropertyValue("", propertyName);
        this.setPrefixedProperty(key, value);
        final PropertySearch instance = this.getInstance();
        final Optional<String> expResult = Optional.ofNullable(this.getPrefixedProperty(key, null));
        final Optional<String> result = instance.find(propertyName, type);
        System.out.println("Expect: " + expResult);
        System.out.println(" Found: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class PropertySearch.
     */
    @Test
    public void testGetProperty_3args() {
        System.out.println("getProperty");
        final Class type = Person.class;
        final String fieldName = "nonExistentField";
        final String propertyName = this.getDefaultPropertyName();
        final String key = propertyName + '.' + Person.class.getName() + '.' + fieldName;
        final String value = this.getPropertyValue(fieldName, propertyName);
        this.setPrefixedProperty(key, value);
        final PropertySearch instance = this.getInstance();
        final Optional<String> expResult = Optional.ofNullable(this.getPrefixedProperty(key, null));
        final Optional<String> result = instance.find(propertyName, type, fieldName);
        System.out.println("Expect: " + expResult);
        System.out.println(" Found: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class PropertySearch.
     */
    @Test
    public void testGetProperty_String_String() {
        System.out.println("getProperty");
        final String fieldName = "dateOfBirth";
        final String propertyName = this.getDefaultPropertyName();
        final String key = propertyName + '.' + fieldName;
        final String value = this.getPropertyValue(fieldName, propertyName);
        this.setPrefixedProperty(key, value);
        final PropertySearch instance = this.getInstance();
        final Optional<String> expResult = Optional.ofNullable(this.getPrefixedProperty(key, null));
        final Optional<String> result = instance.find(propertyName, fieldName);
        System.out.println("Expect: " + expResult);
        System.out.println(" Found: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class PropertySearch.
     */
    @Test
    public void testFind() {
        System.out.println("getProperty");
        final String propertyName = this.getDefaultPropertyName();
        final String value = this.getPropertyValue("", propertyName);
        this.setPrefixedProperty(propertyName, value);

        final PropertySearch instance = this.getInstance();
        Optional<String> expResult = Optional.ofNullable(getPrefixedProperty(propertyName, null));
        Optional<String> result = instance.find(propertyName);
        System.out.println("Expect: " + expResult);
        System.out.println(" Found: " + result);
        assertEquals(expResult, result);

        result = instance.find(propertyName, Person.class);
        System.out.println("Expect: " + expResult);
        System.out.println(" Found: " + result);
        assertEquals(expResult, result);

        result = instance.find(propertyName, "dateOfBirth");
        System.out.println("Expect: " + expResult);
        System.out.println(" Found: " + result);
        assertEquals(expResult, result);

        result = instance.find(propertyName, Person.class, "dateOfBirth");
        System.out.println("Expect: " + expResult);
        System.out.println(" Found: " + result);
        assertEquals(expResult, result);
    }
    
    public String setPersonProperty(String fieldName) {
        return this.setPersonProperty(fieldName, this.getDefaultPropertyName());
    }
    
    public String getDefaultPropertyName() {
        return DEFAULT_PROPERTY_NAME;
    }
    
    public String setPersonProperty(String fieldName, String propertyName) {
        try{
            final Field field = Person.class.getDeclaredField(fieldName);
            final String key = propertyName + '.' + Person.class.getName() + '.' + field.getName();
            final String value = this.getPropertyValue(fieldName, propertyName);
            this.setPrefixedProperty(key, value);
            return key;
        }catch(NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPropertyValue(String fieldName, String propertyName) {
        return this.getPropertyValue(fieldName, propertyName, 
                "SAMPLE VALUE for " + propertyName + '.' + 
                        fieldName + " = " + System.currentTimeMillis());
    }
    
    public String getPropertyValue(String fieldName, String propertyName, String resultIfNone) {
        switch(propertyName) {
            case "format":
                switch(fieldName) {
                    case "dateOfBirth": return "MMy-dd-yyyy";
                    case "name": return "${last} ${first}";
                    default: return resultIfNone;
                }
            default: return resultIfNone;    
        }
    }
    
    public PropertySearch getInstance() {
        return this.getTestConfig().propertySearch(PREFIX, this.getProperties(), ",");
    }
    
    public void setPrefixedProperty(String name, String value) {
        this.setProperty(this.getPrefix() + '.' + name, value);
    }
    
    public void setProperty(String name, String value) {
        this.getProperties().setProperty(name, value);
    }

    public String getPrefixedProperty(String name, String resultIfNone) {
        return this.getProperty(this.getPrefix() + '.' + name, resultIfNone);
    }
    
    public String getPrefix() {
        return PREFIX;
    }
    
    public String getProperty(String name, String resultIfNone) {
        return this.getProperties().getProperty(name, resultIfNone);
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public TestConfig getTestConfig() {
        return new TestConfig();
    }
}
