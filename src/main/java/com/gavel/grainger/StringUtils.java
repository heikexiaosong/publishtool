package com.gavel.grainger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static  final Pattern pattern = Pattern.compile("/([a-zA-Z]-)?(\\d*).html", Pattern.CASE_INSENSITIVE);

    public static String getCode(String url) {
        if ( url==null || url.trim().length()==0 ){
            return "";
        }
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return url;
    }

}
