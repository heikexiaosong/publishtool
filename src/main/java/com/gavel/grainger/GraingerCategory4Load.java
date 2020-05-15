package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.DataSourceHolder;
import com.gavel.entity.GraingerCategory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GraingerCategory4Load {

    public static void main(String[] args) throws Exception {


        Connection conn = DataSourceHolder.dataSource().getConnection();

        Statement stmt = conn.createStatement();


        ResultSet rs = stmt.executeQuery("SELECT * FROM GRAINGERCATEGORY where grade = '3'");
        //遍历结果集

        int i = 0;
        while (rs.next()) {
            System.out.println(rs.getString("url") + "," + rs.getString("code")+ "," + rs.getString("name"));

            loadCategory(conn, rs.getString("url"), rs.getString("code"));

            i += 1;
            System.out.println("Handle: " + i + "\n");

        }

        //释放资源
        stmt.close();
        //关闭连接
        conn.close();

    }

    private static void loadCategory(Connection conn, String url, String parent) throws Exception {


        List<GraingerCategory> graingerBrandList = new ArrayList<>();

        String content = HttpUtils.get("https://www.grainger.cn" + url);

        Document doc = Jsoup.parse(content);

        Elements elements = doc.select("li#liCategoryList div.li_m dd a");

        for (Element element : elements) {
            System.out.println(element);


            GraingerCategory category4 = new GraingerCategory();
            category4.setGrade("4");
            category4.setName(StringUtils.getName(element.text()));
            category4.setParent(parent);
            category4.setUrl(element.attr("href"));
            category4.setCode(StringUtils.getCode(element.attr("href")));

            graingerBrandList.add(category4);


            System.out.println("---");
        }


        if ( graingerBrandList.size() <=0  ) {
            return;
        }

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO GRAINGERCATEGORY VALUES(?, ?, ?, ?, ?)");

        for (GraingerCategory category : graingerBrandList) {
            System.out.println(category);

            //新增
            stmt.setObject(1, category.getCode());
            stmt.setObject(2, category.getName());
            stmt.setObject(3, category.getParent());
            stmt.setObject(4, category.getGrade());
            stmt.setObject(5, category.getUrl());

            stmt.addBatch();
        }



        stmt.executeBatch();

        //释放资源
        stmt.close();


    }
}
