package com.looseboxes.webform.util;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

/**
 * @author hp
 */
public class StringArrayUtilsTest {

    @Test
    public void toArray_givenCommaSeparatedTextWithMissingParts_shouldReturnValidArray() {
        System.out.println("toArray_givenCommaSeparatedTextWithMissingParts_shouldReturnValidArray");
        final String text = this.textWithMissingParts();
        this.toArray_givenCommaSeparatedText_shouldReturnValidArray(text);
    }
    
    @Test
    public void toArray_givenCommaSeparatedText_shouldReturnValidArray() {
        System.out.println("toArray_givenCommaSeparatedText_shouldReturnValidArray");
        final String text = this.text();
        this.toArray_givenCommaSeparatedText_shouldReturnValidArray(text);
    }

    public void toArray_givenCommaSeparatedText_shouldReturnValidArray(String s) {
        final String[] expResult = this.array();
        final String[] result = StringArrayUtils.toArray(s);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void toArrayStream_givenCommaSeparatedText_shouldReturnValidString() {
        System.out.println("toArrayStream");
        final String commaSeparatedText = this.text();
        final Stream<String> expResult = this.stream();
        final Stream<String> result = StringArrayUtils.toStream(commaSeparatedText);
        assertArrayEquals(expResult.toArray(), result.toArray());
    }
    
    public String textWithMissingParts() {
        final String result = "  ,     email_address,username,  mobile_phone_number,password , ";
        return result;
    }
    
    public String text() {
        final String result = "email_address,username,mobile_phone_number,password";
        return result;
    }
    
    public Stream<String> stream() {
        return Stream.of(this.array());
    }
    
    public String [] array() {
        final String[] result = new String[]{
                "email_address", "username","mobile_phone_number", "password"};
        return result;
    }
}
