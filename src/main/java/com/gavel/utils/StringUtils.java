package com.gavel.utils;

public class StringUtils {

    public static String trim(String value) {
        if ( value==null ) {
            return "";
        }
        return value.trim();
    }

    public static boolean isBlank(String value) {
        if ( value==null ) {
            return true;
        }
        return value.trim().length()==0;
    }
}
