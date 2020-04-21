package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;
import java.util.List;

public class GraingerHtmlLoad {

    public static void main(String[] args) throws Exception {

        List<Product> products =  SQLExecutor.executeQueryBeanList("select * from graingerproduct where type = 'g' order by code desc", Product.class);
        System.out.println("Product: "  + products.size());


        long start = 0;
        int i = 0;

        int cnt = 0;
        for (Product product : products) {

            if ( "u".equalsIgnoreCase(product.getType()) ) {
                continue;
            }

            System.out.println(product.getName() + " : " + product.getUrl());

            HtmlCache cache = null;

            List<HtmlCache> exists =  SQLExecutor.executeQueryBeanList("select * from htmlcache  where url = '" + product.getUrl() + "' limit 1 ", HtmlCache.class);
            if ( exists!=null && exists.size()>0 ){
                cache = exists.get(0);
            }

            if ( cache==null ) {
                try {
                    start = System.currentTimeMillis();
                    cache = load(product.getUrl(), product.getCategory());
                    cnt++;
                    if ( cache==null ) {
                        continue;
                    }

                    SQLExecutor.insert(cache);
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


            if ( cache!=null ) {
                Document doc = Jsoup.parse(cache.getHtml());

                Elements tdItemNos = null;
                Element loadmore = doc.selectFirst("div.loadMoreBox a.loadmore");
                if ( loadmore!=null  ) {
                    Element token = doc.selectFirst("input[name='__RequestVerificationToken']");
                    String moreSku = "https://www.grainger.cn/Ajax/GetSkuListTable?__RequestVerificationToken=" + token.attr("value") + "&id=" + product.getCode();
                    doc = Jsoup.parse(HttpUtils.get(moreSku));
                    tdItemNos = doc.select("td[name='tdItemNo'] span a");
                } else {
                    Element leftTable2 = doc.selectFirst("div#leftTable2");
                    tdItemNos = leftTable2.select("td[name='tdItemNo'] span a");
                }
                System.out.println("SKU: " + tdItemNos.size() + "\n");

                int curCnt = 0;
                for (Element tdItemNo : tdItemNos) {

                    curCnt++;

                    String url = "https://www.grainger.cn" + tdItemNo.attr("href");

                    int count = SQLExecutor.intQuery("select count(1) from htmlcache where url = ? ", url);
                    if ( count >=1 ) {
                        continue;
                    }

                    try {
                        start = System.currentTimeMillis();
                        cache = load(url, product.getCategory());
                        cnt++;
                        if ( cache==null ) {
                            continue;
                        }

                        SQLExecutor.insert(cache);
                        System.out.println("\t" + curCnt + ". " +  tdItemNo.text()  + " : " + product.getBrand() + " " + product.getName() + "[URL: " + url + "] ==>  Cost: " + (System.currentTimeMillis() - start) + " ms.");
                    } catch (Exception e) {
                        System.out.println( "\t[异常]"  + " : " + product.getBrand() + " " + product.getName() + " ==>  " + e.getMessage());
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
            }
        }

        System.out.println("Total: " +products.size() + "\n");
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
