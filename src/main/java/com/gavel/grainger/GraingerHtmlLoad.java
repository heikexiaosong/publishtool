package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.DataSourceHolder;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraingerHtmlLoad {

    public static void main(String[] args) throws Exception {


        Connection conn = DataSourceHolder.dataSource().getConnection();

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO htmlcache VALUES(?, ?, ?, ?)");

        PreparedStatement _exist = conn.prepareStatement("select count(1) from htmlcache where url = ? ");

        List<Product> products =  SQLExecutor.executeQueryBeanList("select * from graingerproduct order by code desc", Product.class);
        System.out.println("Product: "  + products.size());


        long start = 0;
        int i = 0;

        int cnt = 0;
        for (Product product : products) {

            if ( "g".equalsIgnoreCase(product.getType()) ) {
                continue;
            }

            HtmlCache cache = null;

            List<HtmlCache> exists =  SQLExecutor.executeQueryBeanList("select * from htmlcache  where url = '" + product.getUrl() + "' ", HtmlCache.class);
            if ( exists!=null && exists.size()>0 ){
                cache = exists.get(0);
            }

            try {
                start = System.currentTimeMillis();
                cache = load(product.getUrl(), product.getCategory());
                cnt++;
                if ( cache==null ) {
                    continue;
                }

                stmt.setObject(1,  cache.getUrl());
                stmt.setObject(2,  cache.getHtml());
                stmt.setObject(3,  cache.getContentlen());
                stmt.setObject(4,  cache.getUpdatetime());

                stmt.execute();
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

            if ( cache!=null ) {
                Document doc = Jsoup.parse(cache.getHtml());
                Element leftTable2 = doc.selectFirst("div#leftTable2");
                Elements tdItemNos = leftTable2.select("td[name='tdItemNo'] a");
                for (Element tdItemNo : tdItemNos) {
                    System.out.println(tdItemNo.attr("href") + "-" +  tdItemNo.text());
                    System.out.println("---");
                }
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
