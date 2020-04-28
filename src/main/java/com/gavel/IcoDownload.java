package com.gavel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class IcoDownload {

    public static void main(String[] args) throws IOException {
        String content = HttpUtils.get("https://www.grainger.cn/");

        Document doc = Jsoup.parse(content);

        Elements two_wz = doc.select("div.beside li.two_wz");

        for (Element element : two_wz) {
            System.out.println(element);

            String name = element.selectFirst("span.wz").text().replace(" ", "_");

            System.out.println(element.selectFirst("span.wz").text());
            System.out.println(name);

            Elements imgs = element.select("span.tp img");

            String mianPic1 = imgs.get(0).attr("src");
            if ( mianPic1!=null && mianPic1.trim().length()>0 ) {
                if ( mianPic1.startsWith("//") ) {
                    mianPic1 = "https:" + mianPic1;
                }
            }

            HttpUtils.download(mianPic1, "pics\\" + name + "_1.png");

            String mianPic2 = imgs.get(1).attr("src");
            if ( mianPic2!=null && mianPic2.trim().length()>0 ) {
                if ( mianPic2.startsWith("//") ) {
                    mianPic2 = "https:" + mianPic2;
                }
            }

            HttpUtils.download(mianPic2, "pics\\" + name + "_2.png");


        }

    }
}
