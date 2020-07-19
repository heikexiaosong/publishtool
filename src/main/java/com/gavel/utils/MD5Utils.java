package com.gavel.utils;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;

public class MD5Utils {

    private static final String SALT = "0FDFA5E5A88BEBAE640A5D88E7C84708";


    /**
     * 使用Apache的Hex类实现Hex(16进制字符串和)和字节数组的互转
     */
    public static String md5Hex(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(str.getBytes());
            return new String(new Hex().encode(digest));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
            return "";
        }
    }


    public static void main(String[] args) {
        System.out.println(md5Hex("https://item.jd.com/5283992.html"));
    }

}
