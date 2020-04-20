package com.gavel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Detail {

    public static void main(String[] args) {

        String content = HttpUtils.get("https://www.grainger.cn//g-673739.html");

        Document doc = Jsoup.parse(content);

        Element leftTable2 = doc.selectFirst("div#leftTable2");

        Elements tdItemNos = leftTable2.select("td[name='tdItemNo'] span a");

        for (Element tdItemNo : tdItemNos) {
            System.out.println(tdItemNo.attr("href") + "-" +  tdItemNo.text());
            System.out.println("---");
        }

    }
}
