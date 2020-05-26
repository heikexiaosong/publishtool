package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.entity.GraingerBrand;
import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class GraingerBrandLoad {

    public static void main(String[] args) throws Exception {


        List<GraingerBrand> graingerBrandList = new ArrayList<>();

        String content = HttpUtils.get("https://www.grainger.cn/brandindex.html");

        Document doc = Jsoup.parse(content);

        Elements elements = doc.select("dl.clearfix dd");



        for (Element element : elements) {
            GraingerBrand graingerBrand = new GraingerBrand();

            Element logo = element.selectFirst("a img");
            graingerBrand.setLogo(logo.attr("data-original"));

            Element brand = element.selectFirst("h3 a");

            String href = brand.attr("href");
            graingerBrand.setCode(StringUtils.getCode(href));
            graingerBrand.setUrl("https://www.grainger.cn" + href);

            Elements childrens = brand.children();
            if ( childrens==null || childrens.size() <= 0 ) {
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


            System.out.println(new Gson().toJson(graingerBrand));
            System.out.println("---");
        }


//        for (GraingerBrand graingerBrand : graingerBrandList) {
//            try {
//                SQLExecutor.insert(graingerBrand);
//            } catch (Exception e) {
//                System.out.println(graingerBrand.getName1() + ": " + e.getMessage());
//            }
//        }


    }
}
