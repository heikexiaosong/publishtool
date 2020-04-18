package com.gavel.grainger;

import com.gavel.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GraingerBrandLoad {

    //数据库连接URL，当前连接的是E:/H2目录下的gacl数据库
    private static final String JDBC_URL = "jdbc:h2:./abc";
    //连接数据库时使用的用户名
    private static final String USER = "sa";
    //连接数据库时使用的密码
    private static final String PASSWORD = "sa";
    //连接H2数据库时使用的驱动类，org.h2.Driver这个类是由H2数据库自己提供的，在H2数据库的jar包中可以找到
    private static final String DRIVER_CLASS="org.h2.Driver";

    public static void main(String[] args) throws Exception {


        List<GraingerBrand> graingerBrandList = new ArrayList<>();

        String content = HttpUtils.get("https://www.grainger.cn/brandindex.html");

        Document doc = Jsoup.parse(content);

        Elements elements = doc.select("dl.clearfix dd");



        for (Element element : elements) {
            System.out.println(element);

            GraingerBrand graingerBrand = new GraingerBrand();

            Element logo = element.selectFirst("a img");
            System.out.println(logo.attr("data-original"));

            graingerBrand.setLogo(logo.attr("data-original"));

            Element brand = element.selectFirst("h3 a");

            String href = brand.attr("href");
            graingerBrand.setCode(StringUtils.getCode(href));
            graingerBrand.setUrl("https://www.grainger.cn" + href);

            Elements childrens = brand.children();
            if ( childrens==null || childrens.size() <= 0 ) {
                System.out.println(brand.text());

                graingerBrand.setName1(brand.text());
                graingerBrand.setName2(brand.text());

            } else {

                for (int i = 0; i < childrens.size(); i++) {
                    Element children = childrens.get(i);
                    if ( i==0 ) {
                        graingerBrand.setName1(children.text());
                    } else  if ( i==1 ) {
                        graingerBrand.setName2(children.text());
                    }
                }

            }

            graingerBrandList.add(graingerBrand);


            System.out.println("---");
        }


        for (GraingerBrand graingerBrand : graingerBrandList) {
            System.out.println(graingerBrand);
        }


        //加载驱动
        Class.forName(DRIVER_CLASS);
        //根据连接URL，用户名，密码，获取数据库连接
        Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO BRAND_GRAINGER VALUES(?, ?, ?, ?, ?)");


        for (GraingerBrand graingerBrand : graingerBrandList) {
            System.out.println(graingerBrand);

            //新增
            stmt.setObject(1, graingerBrand.getCode());
            stmt.setObject(2, graingerBrand.getName1());
            stmt.setObject(3, graingerBrand.getName2());
            stmt.setObject(4, graingerBrand.getLogo());
            stmt.setObject(5, graingerBrand.getUrl());

            stmt.addBatch();
        }



        stmt.executeBatch();

        //释放资源
        stmt.close();
        //关闭连接
        conn.close();


    }
}
