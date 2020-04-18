package com.gavel.grainger;

import com.gavel.HttpUtils;
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

    //数据库连接URL，当前连接的是E:/H2目录下的gacl数据库
    private static final String JDBC_URL = "jdbc:h2:./abc";
    //连接数据库时使用的用户名
    private static final String USER = "sa";
    //连接数据库时使用的密码
    private static final String PASSWORD = "sa";
    //连接H2数据库时使用的驱动类，org.h2.Driver这个类是由H2数据库自己提供的，在H2数据库的jar包中可以找到
    private static final String DRIVER_CLASS="org.h2.Driver";

    public static void main(String[] args) throws Exception {


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
            category.setTitle(hashDivTit.text());
            category.setUrl(hashDivTit.attr("href"));
            category.setCode(hashDivTit.attr("href"));

            graingerBrandList.add(category);

            Elements elements1 = element.select("li.clearfix");
            for (Element element1 : elements1) {
                Element li_1 = element1.selectFirst("div.li_l a");
                System.out.println("\t" + li_1.text());


                GraingerCategory category2 = new GraingerCategory();
                category2.setGrade("2");
                category2.setTitle(li_1.text());
                category2.setUrl(li_1.attr("href"));
                category2.setCode(li_1.attr("href"));

                graingerBrandList.add(category2);


                Elements li_r = element1.select("div.li_r dd a");
                for (Element element2 : li_r) {

                    System.out.println("\t" + element2.text());


                    GraingerCategory category3 = new GraingerCategory();
                    category3.setGrade("3");
                    category3.setTitle(element2.text());
                    category3.setUrl(element2.attr("href"));
                    category3.setCode(element2.attr("href"));

                    graingerBrandList.add(category3);

                }

            }

//            GraingerBrand graingerBrand = new GraingerBrand();
//
//            Element logo = element.selectFirst("a img");
//            System.out.println(logo.attr("data-original"));
//
//            graingerBrand.setLogo(logo.attr("data-original"));
//
//            Element brand = element.selectFirst("h3 a");
//
//
//            graingerBrand.setUrl("https://www.grainger.cn" + brand.attr("href"));
//
//            Elements childrens = brand.children();
//            if ( childrens==null || childrens.size() <= 0 ) {
//                System.out.println(brand.text());
//
//                graingerBrand.setName1(brand.text());
//                graingerBrand.setName2(brand.text());
//
//            } else {
//
//                for (int i = 0; i < childrens.size(); i++) {
//                    Element children = childrens.get(i);
//                    if ( i==0 ) {
//                        graingerBrand.setName1(children.text());
//                    } else  if ( i==1 ) {
//                        graingerBrand.setName2(children.text());
//                    }
//                }
//
//            }
//
//            graingerBrandList.add(graingerBrand);


            System.out.println("---");
        }

        for (GraingerCategory category : graingerBrandList) {
            System.out.println(category);
        }


//        for (GraingerBrand graingerBrand : graingerBrandList) {
//            System.out.println(graingerBrand);
//        }
//
//
        //加载驱动
        Class.forName(DRIVER_CLASS);
        //根据连接URL，用户名，密码，获取数据库连接
        Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);

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
        //关闭连接
        conn.close();


    }
}
