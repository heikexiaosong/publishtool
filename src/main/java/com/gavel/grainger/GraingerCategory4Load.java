package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.suning.SuningClient;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SelectSuningResponse;
import com.suning.api.entity.item.BrandQueryRequest;
import com.suning.api.entity.item.BrandQueryResponse;
import com.suning.api.exception.SuningApiException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GraingerCategory4Load {

    //数据库连接URL，当前连接的是E:/H2目录下的gacl数据库
    private static final String JDBC_URL = "jdbc:h2:./abc";
    //连接数据库时使用的用户名
    private static final String USER = "sa";
    //连接数据库时使用的密码
    private static final String PASSWORD = "sa";
    //连接H2数据库时使用的驱动类，org.h2.Driver这个类是由H2数据库自己提供的，在H2数据库的jar包中可以找到
    private static final String DRIVER_CLASS="org.h2.Driver";

    public static void main(String[] args) throws Exception {


        //加载驱动
        Class.forName(DRIVER_CLASS);
        //根据连接URL，用户名，密码，获取数据库连接
        Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);

        Statement stmt = conn.createStatement();


        ResultSet rs = stmt.executeQuery("SELECT * FROM CATEGORY_GRAINGER where grade = '3'");
        //遍历结果集

        int i = 0;
        while (rs.next()) {
            System.out.println(rs.getString("url") + "," + rs.getString("code")+ "," + rs.getString("title"));

            loadCategory(conn, rs.getString("url"));

            i += 1;
            System.out.println("Handle: " + i + "\n");

        }

        //释放资源
        stmt.close();
        //关闭连接
        conn.close();

    }

    private static void loadCategory(Connection conn, String url) throws Exception {


        List<GraingerCategory> graingerBrandList = new ArrayList<>();

        String content = HttpUtils.get("https://www.grainger.cn" + url);

        Document doc = Jsoup.parse(content);

        Elements elements = doc.select("li#liCategoryList div.li_m dd a");

        for (Element element : elements) {
            System.out.println(element);


            GraingerCategory category4 = new GraingerCategory();
            category4.setGrade("4");
            category4.setTitle(element.text());
            category4.setUrl(element.attr("href"));
            category4.setCode(element.attr("href"));

            graingerBrandList.add(category4);


            System.out.println("---");
        }


        if ( graingerBrandList.size() <=0  ) {
            return;
        }

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO CATEGORY_GRAINGER VALUES(?, ?, ?, ?)");

        for (GraingerCategory category : graingerBrandList) {
            System.out.println(category);

            //新增
            stmt.setObject(1, category.getCode());
            stmt.setObject(2, category.getTitle());
            stmt.setObject(3, category.getGrade());
            stmt.setObject(4, category.getUrl());

            stmt.addBatch();
        }



        stmt.executeBatch();

        //释放资源
        stmt.close();


    }
}
