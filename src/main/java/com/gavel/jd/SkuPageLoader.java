package com.gavel.jd;

import com.gavel.HttpUtils;
import com.gavel.crawler.DriverHtmlLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import com.gavel.entity.PHtmlCache;
import com.gavel.entity.SearchItem;
import com.gavel.utils.MD5Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;

public class SkuPageLoader {

    private static final SkuPageLoader loader = new SkuPageLoader();

    public static SkuPageLoader getInstance() {
        return loader;
    }


    public Item loadPage(SearchItem searchItem) throws Exception {
        if ( searchItem==null || com.gavel.utils.StringUtils.isBlank(searchItem.getCode())) {
            System.out.println("SearchItem is null");
            return null;
        }

        boolean exist = true;

        Item item = SQLExecutor.executeQueryBean("select * from ITEM  where CODE = ? ", Item.class, searchItem.getCode());
        if ( item==null  ){
            item = new Item();
            exist = false;
        }
        if (com.gavel.utils.StringUtils.isNotBlank(item.getBrandname())) {
            return item;
        }

        String id = MD5Utils.md5Hex(searchItem.getUrl());
        String suffix =  id.substring(id.length()-1);

        HtmlCache cache = SQLExecutor.executeQueryBean("select * from htmlcache_"+ com.gavel.utils.StringUtils.trim(suffix) + "  where ID = ? limit 1 ", HtmlCache.class, id);
        if ( cache!=null ) {
            try {
                parseSku(searchItem.getCode(), cache.getHtml(), item);
                return item;
            } catch (Exception e) {
                System.out.println("[Delete Cache][" + searchItem.getUrl() + "]: " + e.getMessage());
                SQLExecutor.execute("delete from htmlcache_"+ com.gavel.utils.StringUtils.trim(suffix) + "  where ID = ? ", id);
            }
        }




        String html = DriverHtmlLoader.getInstance().loadHtml(searchItem.getUrl());
        Thread.sleep(5000);
        if (com.gavel.utils.StringUtils.isNotBlank(html)) {
            try {
                parseSku(searchItem.getCode(), html, item);

                PHtmlCache _cache = new PHtmlCache();
                _cache.setId(MD5Utils.md5Hex(searchItem.getUrl().trim()));
                _cache.setUrl(searchItem.getUrl().trim());
                _cache.setHtml(html);
                _cache.setContentlen(html.length());
                _cache.setUpdatetime(Calendar.getInstance().getTime());
                SQLExecutor.insert(_cache, _cache.getId().substring(_cache.getId().length()-1));
            } catch (Exception e) {
                System.out.println("[" + searchItem.getUrl() + "]: " + e.getMessage());
            }
        }

        if (  item!=null ) {
            if ( exist ) {
                SQLExecutor.update(item);
            } else {
                SQLExecutor.insert(item);
            }

        }

        return item;
    }

    private static Item parseSku(String code, String html, Item skuItem) throws Exception {

        if ( code==null || code.trim().length()==0 ) {
            return null;
        }

        if ( skuItem==null ) {
            skuItem = new Item();
        }

        skuItem.setCode(code);

        String url = code;
        if ( html==null ||  html.trim().length()==0 ) {
            throw new Exception("[" + url + "]获取Html页面异常");
        }


        Document doc = Jsoup.parse(html);

        Element crumb = doc.selectFirst("div#crumb-wrap .crumb");
        if ( crumb==null ) {
            throw new Exception("[" + url + "]Html内容有异常");
        }

        crumb.select("div.sep").remove();


        skuItem.setCategory(crumb.children().get(2).text());
        skuItem.setCategoryname(crumb.children().get(2).text());

        String categoryName = crumb.children().get(0).text() + "|" + crumb.children().get(1).text() + "|" + crumb.children().get(2).text();
        skuItem.setCategorydesc(categoryName);

        String brandName = crumb.children().get(3).text();
        skuItem.setBrandname(brandName);
        skuItem.setBrand(brandName);

        skuItem.setName(crumb.children().get(4).text());


        // 图片
        Elements imgs = doc.select("div#spec-list li>img");
        for (Element img : imgs) {
            System.out.println(img.attr("src") + "\t" + img.attr("data-url"));
        }
        System.out.println("Images: " + imgs.size());

        //

        Element itemInfo = doc.selectFirst("div.itemInfo-wrap");
        System.out.println(itemInfo.select("div.sku-name").text());
        System.out.println(itemInfo.select("div.news #p-ad").text());


        Element detail = doc.selectFirst("div#detail");

        System.out.println(detail.html());
        if ( detail!=null ) {
            Element parameter = detail.selectFirst("div.p-parameter");

            Element brand = parameter.selectFirst("ul#parameter-brand a");
            System.out.println(brand.text());

            Elements parameter2 = parameter.select("ul.parameter2  li");
            for (Element element : parameter2) {
                System.out.println(element.text());
            }


            Element itemdetail = detail.selectFirst("div.item-detail");
            if ( itemdetail!=null ) {
                System.out.println(itemdetail.text());
            }


            // 规格与包装
            Element ptableItem = detail.selectFirst("div.Ptable-item dl");
            if ( ptableItem!=null ) {
                System.out.println(ptableItem.text());
            }


            // package-list
            Element packagelist = detail.selectFirst("div.package-list");
            if ( packagelist!=null ) {
                System.out.println(packagelist.text());
            }



        }


        return skuItem;
    }

    public static void main(String[] args) throws Exception {

        String code = "100002801803";

        String url = "https://i-item.jd.com/" + code + ".html";

        String id = MD5Utils.md5Hex(url);
        String suffix =  id.substring(id.length()-1);

        Item item = null;
        PHtmlCache cache = SQLExecutor.executeQueryBean("select * from htmlcache_"+ com.gavel.utils.StringUtils.trim(suffix) + "  where ID = ? limit 1 ", PHtmlCache.class, id);
        if ( cache!=null ) {
            try {
                item = parseSku(code, cache.getHtml(), null);
                System.out.println("Load From Cache");
            } catch (Exception e) {
                SQLExecutor.execute("delete from htmlcache_"+ com.gavel.utils.StringUtils.trim(suffix) + "  where ID = ? ", id);
                cache = null;
            }
        }

        if ( item==null ) {
            String html = HttpUtils.get(url, "");
            if (com.gavel.utils.StringUtils.isNotBlank(html)) {

                try {
                    item = parseSku(code, html, null);
                    PHtmlCache _cache = new PHtmlCache();
                    _cache.setId(MD5Utils.md5Hex(url.trim()));
                    _cache.setUrl(url.trim());
                    _cache.setHtml(html);
                    _cache.setContentlen(html.length());
                    _cache.setUpdatetime(Calendar.getInstance().getTime());
                    SQLExecutor.insert(_cache, _cache.getId().substring(_cache.getId().length()-1));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }



    }
}
