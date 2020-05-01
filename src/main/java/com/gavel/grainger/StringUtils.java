package com.gavel.grainger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern CODE_PATTERN = Pattern.compile("/([a-zA-Z]-)?(.*).html", Pattern.CASE_INSENSITIVE);

    private static final Pattern name_pattern = Pattern.compile("([^（]*).*", Pattern.CASE_INSENSITIVE);

    public static String getCode(String url) {
        if ( url==null || url.trim().length()==0 ){
            return "";
        }
        Matcher matcher = CODE_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return url;
    }

    public static String getName(String url) {
        if ( url==null || url.trim().length()==0 ){
            return "";
        }
        Matcher matcher = name_pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

}
