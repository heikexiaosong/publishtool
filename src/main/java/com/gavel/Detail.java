package com.gavel;

import okhttp3.OkHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class Detail {

    public static void main(String[] args) {


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        String content = HttpUtils.get("https://www.grainger.cn//g-673739.html", okHttpClient);

        Document doc = Jsoup.parse(content);

        Element loadmore = doc.selectFirst("div.loadMoreBox a.loadmore");
        if ( loadmore!=null  ) {
            Element token = doc.selectFirst("input[name='__RequestVerificationToken']");
            System.out.println(loadmore);
            System.out.println(token.attr("value"));

            String moreSku = "https://www.grainger.cn/Ajax/GetSkuListTable?__RequestVerificationToken=" + token.attr("value") + "&id=673739";
            content = HttpUtils.get(moreSku);

            doc = Jsoup.parse(content);

            Elements tdItemNoList = doc.select("td[name='tdItemNo'] span a");
            for (Element tdItemNo : tdItemNoList) {
                System.out.println(tdItemNo.attr("href") + "-" +  tdItemNo.text());
            }
            System.out.println(tdItemNoList.size());

        } else {
            Element leftTable2 = doc.selectFirst("div#leftTable2");

            Elements tdItemNos = leftTable2.select("td[name='tdItemNo'] span a");

            for (Element tdItemNo : tdItemNos) {
                System.out.println(tdItemNo.attr("href") + "-" +  tdItemNo.text());
                System.out.println("---");
            }

            System.out.println(tdItemNos.size());
        }

    }
}
