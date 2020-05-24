package com.looseboxes.webform.util;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hp
 */
public class StringArrayUtils {

    public static String [] toArray(String s) {
        final String [] output = toStream(s)
                .collect(Collectors.toList()).toArray(new String[0]);
        return output;
    }
    
    public static Stream<String> toStream(String commaSeparatedText) {
        final Stream<String> output;
        if(commaSeparatedText == null) {
            output = Stream.empty();
        }else if(commaSeparatedText.isEmpty()) {
            output = Stream.empty();
        }else if(commaSeparatedText.indexOf(',') == -1) {
            output = Stream.of(commaSeparatedText.trim());
        }else{
            output = Arrays.asList(commaSeparatedText.split(",")).stream()
                    .map((e) -> e == null ? null : e.trim())
                    .filter((e) -> e != null && !e.isEmpty());
        }
        return output;
    }
}
