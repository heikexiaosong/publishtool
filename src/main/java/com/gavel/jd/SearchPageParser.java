package com.gavel.jd;

import com.gavel.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchPageParser {

    public static void main(String[] args) {
        System.out.println("JD Search Page");

        String html = HttpUtils.get("https://i-list.jd.com/list.html?cat=14065,14113,14115", "");


        Document doc = Jsoup.parse(html);

        System.out.println(doc.selectFirst("div.f-pager .fp-text i"));
        System.out.println(doc.selectFirst("div.f-result-sum span.num"));

        Elements items = doc.select("div#plist li.gl-item div.j-sku-item");

        for (Element item : items) {
            System.out.println(item.selectFirst("div.p-name em").text());

            System.out.print(item.attr("data-sku"));
            System.out.print("\t" + item.attr("venderid"));
            System.out.print("\t" + item.attr("jdzy_shop_id"));
            System.out.println("\t" + item.attr("brand_id"));

            System.out.println(item.selectFirst("div.p-price"));
        }

    }
}
