package com.gavel;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.utils.StringUtils;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoTest {

    public static void main(String[] args) {
        //连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("localhost", 27017);

        //连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("jingsu");

        MongoCollection<Document> collection = mongoDatabase.getCollection("htmlcache");

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

                    try {
                        htmlCache =  SQLExecutor.executeQueryBean(" select URL, HTML from HTMLCACHE where URL = ? ", HtmlCache.class, htmlCache.getUrl());
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                    if ( htmlCache==null || StringUtils.isBlank(htmlCache.getHtml())) {
                        continue;
                    }


                    //创建文档
                    Document document = new Document("url", htmlCache.getUrl())
                            .append("html", htmlCache.getHtml())
                            .append("length",  htmlCache.getContentlen());


                    collection.insertOne(document);

                    System.out.print("\r" + i + "/" + htmlCaches.size() + ": " + htmlCache.getUrl());
                }
            }

            if ( htmlCaches.size() < size ) {
                break;
            }

            offset += size;
        }





    }
}
