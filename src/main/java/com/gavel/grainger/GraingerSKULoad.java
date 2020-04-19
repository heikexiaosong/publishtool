package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.DataSourceHolder;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Product;
import com.google.common.io.Files;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GraingerSKULoad {

    public static void main(String[] args) throws Exception {


        Connection conn = DataSourceHolder.dataSource().getConnection();

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO htmlcache VALUES(?, ?, ?, ?)");

//        PreparedStatement _exist = conn.prepareStatement("select count(1) from htmlcache where url = ? ");

        List<Product> products =  SQLExecutor.executeQueryBeanList("select * from graingerproduct order by code desc", Product.class);
        System.out.println("Product: "  + products.size());

        /**
         * List -> Map
         * 需要注意的是：
         * toMap 如果集合对象有重复的key，会报错Duplicate key ....
         *  apple1,apple12的id都为1。
         *  可以用 (k1,k2)->k1 来设置，如果有重复的key,则保留key1,舍弃key2
         */
        Map<String, Product> urlProductMap = products.stream().collect(Collectors.toMap(Product::getUrl, a -> a));

        List<HtmlCache> caches =  SQLExecutor.executeQueryBeanList("select URL from htmlcache ", HtmlCache.class);
        System.out.println("HtmlCache: "  + caches.size());

        caches.stream().forEach( e -> urlProductMap.remove(e.getUrl()) );
        System.out.println("task: "  + urlProductMap.size());

        long start = 0;
        int i = 0;

        int cnt = 0;
        for (Product product : urlProductMap.values()) {

            try {
                start = System.currentTimeMillis();
                HtmlCache cache = load(product.getUrl(), product.getCategory());
                cnt++;
                if ( cache==null ) {
                    continue;
                }

                stmt.setObject(1,  cache.getUrl());
                stmt.setObject(2,  cache.getHtml());
                stmt.setObject(3,  cache.getContentlen());
                stmt.setObject(4,  cache.getUpdatetime());

                stmt.execute();
                caches.add(cache);
                System.out.println( i  + " : " + product.getBrand() + " " + product.getName() + " ==>  Cost: " + (System.currentTimeMillis() - start) + " ms.");
                i++;
            } catch (Exception e) {
                System.out.println( "\t[异常]" + (i++)  + " : " + product.getBrand() + " " + product.getName() + " ==>  " + e.getMessage());
                HttpUtils.cache.clear();
                if ( cnt < 5 ) {
                    Thread.sleep(180000);
                } else if ( cnt < 50 ) {
                    Thread.sleep(60000);
                } else  if ( cnt < 500 ) {
                    Thread.sleep(30000);
                } else {
                    Thread.sleep(10000);
                }
                cnt = 0;
            }
        }

        System.out.println("Total: " +products.size());

        //释放资源
        stmt.close();
        //关闭连接
        conn.close();


    }

    public static HtmlCache load(String url, String category) throws Exception {

        if ( url==null || url.trim().length() <= 0 ) {
            return null;
        }

        String content = HttpUtils.get(url.trim(), "https://www.grainger.cn/c-" + category + ".html");
        if ( content==null || content.trim().length()==0 ) {
            return null;
        }

        HtmlCache cache = new HtmlCache();
        cache.setUrl(url.trim());
        cache.setHtml(content);
        cache.setContentlen(content.length());
        cache.setUpdatetime(Calendar.getInstance().getTime());
        return cache;

    }

}
