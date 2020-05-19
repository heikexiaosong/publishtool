package com.gavel.utils;

public class URLUtils {

    public static String getName(String url) {
        int start = url.lastIndexOf("/") + 1;
        int end = url.lastIndexOf("?");
        if ( end == -1 ) {
            end = url.trim().length();
        }
        return url.substring(start, end);
    }

    public static void main(String[] args) {
        System.out.println(getName("https://www.grainger.cn/g-397488.html"));
    }
}
