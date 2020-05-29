package com.gavel;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MongoTest {

    public static void main(String[] args) {

        final  int size = 500;
        int offset = 0;
        while (true) {
            List<HtmlCache> htmlCaches = new ArrayList<>();
            try {
                htmlCaches =  SQLExecutor.executeQueryBeanList(" select URL from HTMLCACHE order by URL limit ?, ? ", HtmlCache.class, offset, size);
            } catch (Exception e) {

                e.printStackTrace();
            }

            System.out.println("\n[Offset: " + offset + "]Size: " + htmlCaches.size());

            if ( htmlCaches!=null ) {
                for (int i = 0; i < htmlCaches.size(); i++) {


                    HtmlCache htmlCache =  htmlCaches.get(i);


                    System.out.print("\r" + i + "/" + htmlCaches.size() + ": " + htmlCache.getUrl());

                    try {
                        htmlCache = SQLExecutor.executeQueryBean("select * from HTMLCACHE where URL = ?", HtmlCache.class, htmlCache.getUrl());
                    } catch (Exception e) {

                    }


                    if (StringUtils.isBlank(htmlCache.getHtml())) {
                        continue;
                    }





                    try {

                        Document doc = Jsoup.parse(htmlCache.getHtml());
                        doc.select("script").remove();

                        htmlCache.setHtml(doc.outerHtml());
                        SQLExecutor.update(htmlCache);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if ( htmlCaches.size() < size ) {
                break;
            }

            offset += size;
        }

    }
}
