package com.gavel.shelves;

import com.gavel.crawler.HtmlPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import com.gavel.entity.ShelvesItem;
import com.gavel.grainger.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ShelvesItemParser {


    public static ShelvesItem parse(Item item) throws Exception {

        if ( item==null || item.getUrl()==null || item.getUrl().trim().length()==0 ) {
            throw new Exception("item 或者 产品URL 不能为空");
        }

        ShelvesItem shelvesItem = new ShelvesItem();

        HtmlCache htmlCache = HtmlPageLoader.getInstance().loadHtmlPage(item.getUrl(), true);
        if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
            throw new Exception("[Item: " + item.getUrl() +"]html获取失败.");
        }

        Document doc = Jsoup.parse(htmlCache.getHtml());

        Element err = doc.selectFirst("div.err-notice");
        if ( err!=null ) {
            throw new Exception("[URL: " + item.getUrl() + "]" + doc.title());
        }


        // 4级类目 + 产品组ID + ID
        Elements elements = doc.select("div.crumbs  a");
        Element c4 = elements.get(4);
        Element c5 = elements.get(5);

        System.out.println(StringUtils.getCode(c4.attr("href")) + ": " + c4.text());

        shelvesItem.setItemCode(item.getCode());
        shelvesItem.setProductName(c5.text());

        Element proDetailCon = doc.selectFirst("div.proDetailCon");

        Element h3 = proDetailCon.selectFirst(" > h3 ");
        h3.remove();

        Element brandCn = h3.selectFirst(" > span a");
        brandCn.remove();
        System.out.println("中文品牌: " + brandCn.text() + ": " + StringUtils.getCode(brandCn.attr("href")));

        Element title = h3.selectFirst(" > a");
        title.remove();
        System.out.println("标题: " + title.text());
        shelvesItem.setCmTitle(title.text());

        Element sellPoint = proDetailCon.selectFirst(" > h4 span");
        sellPoint.remove();
        shelvesItem.setSellingPoints(sellPoint.text());


        Element price = proDetailCon.selectFirst(" > div.price");
        price.remove();
        System.out.println("价格: " + price.text());

        Elements attrs = proDetailCon.select(" > div font");
        attrs.remove();
        System.out.println("\n属性: \n" + attrs.html());



        return shelvesItem;
    }

    public static void main(String[] args) throws Exception {
        Item item = SQLExecutor.executeQueryBean("select * from ITEM where CODE = ?", Item.class, "10D2148");
        ShelvesItem shelvesItem = parse(item);
        System.out.println(shelvesItem);
    }


}
