package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.DataSourceHolder;
import com.gavel.entity.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraingerProductLoad {

    private static final Pattern CODE_PATTERN = Pattern.compile("/([a-zA-Z])-([^\\.]*).html", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) throws Exception {


        List<Product> graingerBrandList = new ArrayList<>();

        String content = HttpUtils.get("https://www.grainger.cn/c-209089.html");

        Document doc = Jsoup.parse(content);

        Elements elements = doc.select("div.proUL li");

        for (Element element : elements) {
            System.out.println(element);

            Product graingerBrand = new Product();

            graingerBrand.setName(element.attr("title"));

            Element item = element.selectFirst("a");

            String href = item.attr("href");

            graingerBrand.setCode(href);
            graingerBrand.setType("");
            graingerBrand.setUrl("https://www.grainger.cn/" + href);

            Matcher matcher = CODE_PATTERN.matcher(href);
            if (matcher.find()) {
                graingerBrand.setCode(matcher.group(2));
                graingerBrand.setType(matcher.group(1));
            }

            String picUrl = item.selectFirst("div.pic img").attr("src");

            if ( "/Content/images/hp_np.png".equalsIgnoreCase(picUrl.trim()) ) {
                graingerBrand.setPic("https://www.grainger.cn/Content/images/hp_np.png");
            } else {
                graingerBrand.setPic("https:" + picUrl);
            }

            graingerBrand.setBrand(item.selectFirst("div.wenz h3 span").text());
            graingerBrand.setCategory("209089");


            graingerBrandList.add(graingerBrand);


            System.out.println("---");
        }


        for (Product product : graingerBrandList) {
            System.out.println(product);
        }


        Connection conn = DataSourceHolder.dataSource().getConnection();

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO GRAINGERPRODUCT VALUES(?, ?, ?, ?, ?, ?, ?)");


        for (Product graingerBrand : graingerBrandList) {
            System.out.println(graingerBrand);

            //新增
            stmt.setObject(1, graingerBrand.getCode());
            stmt.setObject(2, graingerBrand.getType());
            stmt.setObject(3, graingerBrand.getName());
            stmt.setObject(4, graingerBrand.getBrand());
            stmt.setObject(5, graingerBrand.getCategory());
            stmt.setObject(6, graingerBrand.getPic());
            stmt.setObject(7, graingerBrand.getUrl());

            stmt.addBatch();
        }



        stmt.executeBatch();

        //释放资源
        stmt.close();
        //关闭连接
        conn.close();


    }
}
