package com.gavel;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoTest {

    public static void main(String[] args) {
        //连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("localhost", 27017);


        //连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("jingsu");

        MongoCollection<Document> collection = mongoDatabase.getCollection("user");

        //创建文档
        Document document = new Document("name","张三")
                .append("sex", "男")
                .append("age", 18);


        collection.insertOne(document);

    }
}
