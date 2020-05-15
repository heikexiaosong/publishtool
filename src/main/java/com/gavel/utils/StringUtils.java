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

    public static boolean isNotBlank(String value) {
        if ( value==null ) {
            return false;
        }
        return value.trim().length()>0;
    }
}
