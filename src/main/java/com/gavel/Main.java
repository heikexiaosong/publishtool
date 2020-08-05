package com.gavel;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println(Arrays.toString("锐能".split("[（）]")));




    }

    private static String escape(String text) {
        String res = text;
        if ( text!=null && text.contains(",") ) {
            res = "\"" + text + "\"";
        }

        return res;
    }
}
