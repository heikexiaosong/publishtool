package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.DataSourceHolder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class GraingerProductDetailLoad {

    public static void main(String[] args) throws Exception {


        List<GraingerBrand> graingerBrandList = new ArrayList<>();

        String html = HttpUtils.get("https://www.grainger.cn//g-319354.html");

        Document doc = Jsoup.parse(html);

//        Element content = doc.selectFirst("div.content");
//        Element content = doc.selectFirst("div.proDetailDiv");
//
//
//
//        for (Element element : elements) {
//            System.out.println(element);
//
//            GraingerBrand graingerBrand = new GraingerBrand();
//
//            Element logo = element.selectFirst("a img");
//            System.out.println(logo.attr("data-original"));
//
//            graingerBrand.setLogo(logo.attr("data-original"));
//
//            Element brand = element.selectFirst("h3 a");
//
//            String href = brand.attr("href");
//            graingerBrand.setCode(StringUtils.getCode(href));
//            graingerBrand.setUrl("https://www.grainger.cn" + href);
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
//
//
//            System.out.println("---");
//        }


        for (GraingerBrand graingerBrand : graingerBrandList) {
            System.out.println(graingerBrand);
        }
        System.out.println("Total: " + graingerBrandList.size());

//
//        Connection conn = DataSourceHolder.dataSource().getConnection();
//
//        PreparedStatement stmt = conn.prepareStatement("INSERT INTO GRAINGERBRAND VALUES(?, ?, ?, ?, ?)");
//
//
//        for (GraingerBrand graingerBrand : graingerBrandList) {
//            System.out.println(graingerBrand);
//
//            //新增
//            stmt.setObject(1, graingerBrand.getCode());
//            stmt.setObject(2, graingerBrand.getName1());
//            stmt.setObject(3, graingerBrand.getName2());
//            stmt.setObject(4, graingerBrand.getLogo());
//            stmt.setObject(5, graingerBrand.getUrl());
//
//            stmt.addBatch();
//        }
//
//
//
//        stmt.executeBatch();
//
//        //释放资源
//        stmt.close();
//        //关闭连接
//        conn.close();


    }
}
