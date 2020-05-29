package com.gavel.utils;

import com.gavel.crawler.HtmlPageLoader;
import com.gavel.entity.HtmlCache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtil {

    public static String compress(String str) throws IOException {

    if (str == null || str.length() == 0) {

      return str;

    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    GZIPOutputStream gzip = new GZIPOutputStream(out);

    gzip.write(str.getBytes());

    gzip.close();

    return out.toString("ISO-8859-1");

  }

  public static String uncompress(String str) throws IOException {

    if (str == null || str.length() == 0) {

      return str;

    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));

    GZIPInputStream gunzip = new GZIPInputStream(in);

    byte[] buffer = new byte[256];

    int n;

    while ((n = gunzip.read(buffer)) >= 0) {

      out.write(buffer, 0, n);

    }

    return out.toString();

  }

  public static void main(String[] args) throws Exception {


      HtmlCache cache = HtmlPageLoader.getInstance().loadHtmlPage("https://www.grainger.cn/u-10H5595.html11", true);


  String str=cache.getHtml();



  System.out.println("原长度："+str.length()); 



  System.out.println("压缩后："+ZipUtil.compress(str).length()); 



    System.out.println("解压缩："+ZipUtil.uncompress(ZipUtil.compress(str)));

  }
}
