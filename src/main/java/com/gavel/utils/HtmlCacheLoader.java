package com.gavel.utils;

import com.gavel.entity.HtmlCache;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;


public class HtmlCacheLoader {

    private static final HtmlCacheLoader loader = new HtmlCacheLoader();

    public static HtmlCacheLoader getInstance() {
        return loader;
    }


    private final MongoClient mongoClient;

    private final  MongoDatabase mongoDatabase;

    private final MongoCollection<Document> collection;

    private HtmlCacheLoader() {

        mongoClient = new MongoClient("localhost", 27017);

        //连接到数据库
        mongoDatabase = mongoClient.getDatabase("jingsu");

        collection = mongoDatabase.getCollection("htmlcache");

    }

    public HtmlCache loadHtmlCache(String url) {
        HtmlCache htmlCache = null;
        if ( StringUtils.isBlank(url) ) {
            return htmlCache;
        }

        long start = System.currentTimeMillis();
        FindIterable<Document> it = collection.find(Filters.eq("url", url.trim()));

        System.out.println(it.iterator().hasNext());

        Document myDoc = collection.find(Filters.eq("url", url.trim())).first();

        System.out.println(myDoc.getString("html"));


        System.out.println("Cost: " + (System.currentTimeMillis() - start) + "ms");


        return htmlCache;

    }

    public static void main(String[] args) {
        getInstance().loadHtmlCache("https://www.grainger.cn/u-10H5595.html11");
    }


}
