package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.DataSourceHolder;
import com.gavel.entity.GraingerCategory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class GraingerCategoryLoad {

    public static void main(String[] args) throws Exception {

        Connection conn = DataSourceHolder.dataSource().getConnection();

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO GRAINGERCATEGORY VALUES(?, ?, ?, ?, ?)");


        List<GraingerCategory> graingerBrandList = new ArrayList<>();

        String content = HttpUtils.get("https://www.grainger.cn/categoryindex.html");

        Document doc = Jsoup.parse(content);

        Elements elements = doc.select("div.hashDivCon");

        for (Element element : elements) {
            //System.out.println(element);

            System.out.println(element.attr("data-index"));

            Element hashDivTit = element.selectFirst("div.hashDivTit a");

            GraingerCategory category = new GraingerCategory();
            category.setGrade("1");
            category.setName(hashDivTit.text());
            category.setUrl(hashDivTit.attr("href"));
            category.setCode(StringUtils.getCode(hashDivTit.attr("href")));

            //新增
            stmt.setObject(1, category.getCode());
            stmt.setObject(2, category.getName());
            stmt.setObject(3, "");
            stmt.setObject(4, category.getGrade());
            stmt.setObject(5, category.getUrl());

            stmt.addBatch();

            graingerBrandList.add(category);

            Elements elements1 = element.select("li.clearfix");
            for (Element element1 : elements1) {
                Element li_1 = element1.selectFirst("div.li_l a");
                System.out.println("\t" + li_1.text());


                GraingerCategory category2 = new GraingerCategory();
                category2.setGrade("2");
                category2.setName(li_1.text());
                category2.setUrl(li_1.attr("href"));
                category2.setCode(StringUtils.getCode(li_1.attr("href")));

                stmt.setObject(1, category2.getCode());
                stmt.setObject(2, category2.getName());
                stmt.setObject(3, category.getCode());
                stmt.setObject(4, category2.getGrade());
                stmt.setObject(5, category2.getUrl());
                stmt.addBatch();

                graingerBrandList.add(category2);


                Elements li_r = element1.select("div.li_r dd a");
                for (Element element2 : li_r) {

                    System.out.println("\t" + element2.text());


                    GraingerCategory category3 = new GraingerCategory();
                    category3.setGrade("3");
                    category3.setName(element2.text());
                    category3.setUrl(element2.attr("href"));
                    category3.setCode(StringUtils.getCode(element2.attr("href")));

                    stmt.setObject(1, category3.getCode());
                    stmt.setObject(2, category3.getName());
                    stmt.setObject(3, category2.getCode());
                    stmt.setObject(4, category3.getGrade());
                    stmt.setObject(5, category3.getUrl());
                    stmt.addBatch();

                    graingerBrandList.add(category3);

                }

            }


            System.out.println("---");
        }


        stmt.executeBatch();

        //释放资源
        stmt.close();
        //关闭连接
        conn.close();


    }
}
