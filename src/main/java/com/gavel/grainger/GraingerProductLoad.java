package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.DataSourceHolder;
import com.gavel.entity.Product;
import com.google.common.io.Files;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraingerProductLoad {

    private static final Pattern CODE_PATTERN = Pattern.compile("/([a-zA-Z])-([^\\.]*).html", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) throws Exception {


        loadProducts("204888");



    }

    public static void load(String category) throws Exception {


        List<Product> graingerBrandList = loadProducts(category);

        Connection conn = DataSourceHolder.dataSource().getConnection();

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO graingerproduct VALUES(?, ?, ?, ?, ?, ?, ?)");


        for (Product graingerBrand : graingerBrandList) {

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



        int[] rets = stmt.executeBatch();

        System.out.println(Arrays.toString(rets));
        System.out.println("Total: " +rets.length);

        //释放资源
        stmt.close();
        //关闭连接
        conn.close();


    }


    public static List<Product> loadProducts(String categoryCode){

        List<Product> products = new ArrayList<>();
        if ( categoryCode==null || categoryCode.trim().length()==0 ){
            return products;
        }

        int cur = 0;
        int total = 1;

        while ( cur < total ) {
            cur++;

            String content = null;

            String cacheFile = "list" + File.separator + categoryCode + "_" + cur + ".html";

            File cache = new File(cacheFile);
            if ( cache.exists() ) {
                try {
                    content = Files.toString(cache, Charset.forName("UTF8"));
                    System.out.println("Load Content From file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                content = HttpUtils.get("https://www.grainger.cn/c-" + categoryCode + ".html?page=" + cur);

                System.out.println("Load Content From network");
                try {
                    Files.write(content.getBytes(), new File(categoryCode + "_" + cur + ".html"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            Document doc = Jsoup.parse(content);

            Element pagination = doc.selectFirst("div.pagination");
            Elements label = pagination.select("label");
            cur = Integer.parseInt(label.get(0).text());
            total = Integer.parseInt(label.get(1).text());

            System.out.print("[" + categoryCode + "]Cur: " + cur + "; Total: " + total);

            System.out.print("; cpz: " + doc.selectFirst("font.cpz").text());
            System.out.println("; total: " + doc.selectFirst("font.total").text());


            // 产品列表数据
            Elements elements = doc.select("div.proUL li");
            for (Element element : elements) {

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
                graingerBrand.setCategory(categoryCode);


                products.add(graingerBrand);
            }
        }


        return products;
    }



}
