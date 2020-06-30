package com.looseboxes.webform.util;

/**
 * @author hp
 */
public final class StringUtils {
    
    private StringUtils() {}
    
    public static String camelToSnakeCase(String str) {
        final String result;
        if(str == null) {
            result = null;
        }else if(str.isEmpty() || str.length() == 1) {
            result = str;
        }else{
            final StringBuilder buffer = new StringBuilder(str.length() * 2);
            for(int i=0; i<str.length(); i++) {
                char ch = str.charAt(i);
                if(Character.isUpperCase(ch)) {
                    buffer.append('_').append(Character.toLowerCase(ch)); 
                }else{
                    buffer.append(ch);
                }
            }
            result = buffer.toString();
        }
        return result;
    }
    
    public static String snakeToCamelCase(String str) {
        final String result;
        if(str == null) {
            result = null;
        }else if(str.isEmpty() || str.length() == 1) {
            result = str;
        }else {
            final int pos = str.indexOf('_');
            if(pos == -1 || pos == 0) {
                result = str;
            }else{
                final StringBuilder buffer = new StringBuilder(str.length());
                boolean txNext = false;
                for(int i=0; i<str.length(); i++) {
                    char ch = str.charAt(i);
                    if(txNext) {
                        buffer.append(Character.toUpperCase(ch));
                        txNext = false;
                    }else{
                        if(ch == '_') {
                            txNext = true;
                        }else{
                            buffer.append(ch);
                        }
                    }
                }
                result = buffer.toString();
            }
        }
        return result;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
