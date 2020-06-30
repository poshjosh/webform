package com.looseboxes.webform.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author hp
 */
public class StringArrayUtils {

    public static String [] toArray(String text) {
        return toArray(text, ",");
    }

    public static String [] toArray(String text, String separator) {
        return toList(text, separator).toArray(new String[0]);
    }

    public static Stream<String> toStream(String text) {
        return toStream(text, ",");
    }
    
    public static Stream<String> toStream(String text, String separator) {
        final Stream<String> output;
        if(StringUtils.isNullOrEmpty(text)) {
            output = Stream.empty();
        }else if( ! text.contains(separator)) {
            output = Stream.of(text.trim());
        }else{
            output = toList(text, separator).stream();
        }
        return output;
    }

    public static List<String> toList(String text, String separator) {
        final List<String> output;
        if(StringUtils.isNullOrEmpty(text)) {
            output = Collections.EMPTY_LIST;
        }else if( ! text.contains(separator)) {
            output = Collections.singletonList(text.trim());
        }else{
            final String [] parts = text.split(separator);
            if(parts == null || parts.length == 0) {
                output = Collections.singletonList(text.trim());
            }else if(parts.length == 1) {
                output = Collections.singletonList(text.trim());
            }else{
                output = new ArrayList(parts.length);
                for(String part : parts) {
                    part = part == null ? null : part.trim();
                    if(StringUtils.isNullOrEmpty(part)) {
                        continue;
                    }
                    output.add(part);
                }
            }
        }
        return output;
    }
}
