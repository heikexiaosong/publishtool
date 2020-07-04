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

        String categoryName = crumb.children().get(0).text() + "|" + crumb.children().get(1).text() + "|" + crumb.children().get(2).text();
        skuItem.setCategoryname(categoryName);

        String brandName = crumb.children().get(3).text();
        skuItem.setBrandname(brandName);

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

        return skuItem;
    }

    public static void main(String[] args) {

        String html = HttpUtils.get("https://i-list.jd.com/list.html?cat=14065,15394", "");
        if (com.gavel.utils.StringUtils.isNotBlank(html)) {

            Document doc = Jsoup.parse(html);

            Elements brands = doc.select("ul#brandsArea li");

            for (Element brand : brands) {
                System.out.println(brand.outerHtml());
                System.out.println(brand.attr("id"));
                System.out.println(brand.selectFirst("a").attr("title"));
                System.out.println(brand.selectFirst("a").attr("href"));
            }
        }
    }
}
