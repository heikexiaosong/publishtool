package com.gavel.crawler;

import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  爬取列表页面
 */
public class GraingerHtmlInfo {


    private static final Pattern CODE_PATTERN = Pattern.compile("/([a-zA-Z])-([^\\.]*).html", Pattern.CASE_INSENSITIVE);

    public static HtmlCache loadHtmlPage(String url, String params) throws Exception {

        HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, url);
        if ( cache == null ) {
            StringBuilder urlBuilder = new StringBuilder(url.trim());
            if ( params!=null && params.trim().length() > 0 ){
                urlBuilder.append(params.trim());
            }

            String content = HttpUtils.get(urlBuilder.toString().trim(), "https://www.grainger.cn");
            if ( content==null || content.trim().length()==0 ) {
                return null;
            }

            cache = new HtmlCache();
            cache.setUrl(url.trim());
            cache.setHtml(content);
            cache.setContentlen(content.length());
            cache.setUpdatetime(Calendar.getInstance().getTime());
            SQLExecutor.insert(cache);
        }
        return cache;
    }


    public static void main(String[] args) throws Exception {


        String url = "https://www.grainger.cn/s-1.html";


        HtmlCache htmlCache = loadHtmlPage(url, null);

        if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
            System.out.println("[URL: " + url + "]网页打开失败");
            return;
        }


        Document document = Jsoup.parse(htmlCache.getHtml());

        Element cpz = document.selectFirst("font.cpz");
        Element total = document.selectFirst("font.total");


        System.out.println("产品组: " + cpz.text() + "; 产品: " + total.text());

        Elements brands = document.select("div.allbrand dd");

        for (Element brand : brands) {
            System.out.println(brand.attr("brandcode") + ": " + brand.selectFirst("a").text());
        }

        int pageCur = 0;
        int pageTotal = 0;

        Elements labels = document.select("div.pagination > label");
        if ( labels.size()==2 ) {
            pageCur = Integer.parseInt(labels.get(0).text());
            pageTotal = Integer.parseInt(labels.get(1).text());
        }

        System.out.println("当前页: " + pageCur);
        System.out.println("总页数: " + pageTotal);


        List<Product> products = new ArrayList<>();
        List<SkuItem>  skuItems = new ArrayList<>();

        while ( pageCur <= pageTotal ) {
            String pageUrl = "https://www.grainger.cn/s-1.html?page=" + pageCur;
            HtmlCache htmlCache1 = loadHtmlPage(pageUrl, null);

            if ( htmlCache1==null || htmlCache1.getHtml()==null || htmlCache1.getHtml().trim().length()==0 ) {
                System.out.println("[URL: " + pageUrl + "]网页打开失败");
                return;
            }

            Document document1 = Jsoup.parse(htmlCache1.getHtml());


            Elements labels1 = document1.select("div.pagination > label");


            List<Product> products1 = new ArrayList<>();

            // 产品列表数据
            Elements elements = document1.select("div.proUL li");
            for (Element element : elements) {

                Product graingerBrand = new Product();

                graingerBrand.setName(element.attr("title"));

                Element item = element.selectFirst("a");

                String href = item.attr("href");

                graingerBrand.setCode(href);
                graingerBrand.setType("");
                graingerBrand.setUrl("https://www.grainger.cn" + href);

                Matcher matcher = CODE_PATTERN.matcher(href);
                if (matcher.find()) {
                    graingerBrand.setCode(matcher.group(2));
                    graingerBrand.setType(matcher.group(1));
                }

                products1.add(graingerBrand);

                HtmlCache product = loadHtmlPage(graingerBrand.getUrl(), null);

                if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
                    System.out.println("[URL: " + url + "]网页打开失败");
                } else {
                    List<SkuItem>  skuItems1 = GraingerProductParser.parse(product.getHtml());
                    skuItems.addAll(skuItems1);
                    System.out.println("skuItems: " + skuItems.size());
                }


               // System.out.println(graingerBrand);
            }


            System.out.println("当前页: " + labels1.get(0).text() + ": " + products1.size() + "\n" );


            products.addAll(products1);

            pageCur++;
        }

        System.out.println("Total: " + products.size() );

    }



}
