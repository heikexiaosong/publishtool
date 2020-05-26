package com.gavel;

import com.gavel.crawler.DriverHtmlLoader;
import com.gavel.crawler.HtmlPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.BrandInfo;
import com.gavel.entity.HtmlCache;
import com.gavel.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkuNumber {

    //试管 离心管及附件（1774）

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(.*)（(.*)）", Pattern.CASE_INSENSITIVE);


    private static final Pattern CODE_PATTERN = Pattern.compile("/([a-zA-Z]-)?(.*).html\\?b=(.*)", Pattern.CASE_INSENSITIVE);


    public static class Info {
        public String name1;

        public String name2;

        public int count;
    }

    public static Info parse(String text) {



        if ( StringUtils.isBlank(text) ){
            return null;
        }


        Info info = new Info();

        Matcher matcher = NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {

            String name = matcher.group(1);

            String[] arr = StringUtils.trim(name).split(" ");
            if ( arr.length>=1 ) {
                info.name1 = arr[0];
            }
            if ( arr.length>=2 ) {
                info.name2 = arr[1];
            }
            info.count = Integer.parseInt(matcher.group(2));
            return info;
        }
        return info;
    }


    public static String getNumber(String text) {
        if ( StringUtils.isBlank(text) ){
            return "0";
        }
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "0";
    }


    public static String getBrandCode(String url) {
        if ( url==null || url.trim().length()==0 ){
            return "";
        }
        Matcher matcher = CODE_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(3);
        }
        return url;
    }



    public static void main(String[] args) throws Exception {



        //

       //  loadBrandInfo();

        updateBrandInfo();




    }

    private static void updateBrandInfo() throws Exception {

        List<BrandInfo> brandInfoList = new ArrayList<>();

        try {
            brandInfoList = SQLExecutor.executeQueryBeanList("select * from BRAND_INFO order by SKUNUM desc", BrandInfo.class);
        } catch (Exception e) {

        }

        if ( brandInfoList!=null ) {


            for (int i = 0; i < brandInfoList.size(); i++) {
                BrandInfo brandInfo = brandInfoList.get(i);

                String url = brandInfo.getUrl();
                try {
                    HtmlCache htmlCache = DriverHtmlLoader.getInstance().loadHtmlPage(url, false);
                    if ( htmlCache!=null && htmlCache.getHtml()!=null ) {

                        Document document = Jsoup.parse(htmlCache.getHtml());
                        Element cpz = document.selectFirst("font.cpz");
                        Element total = document.selectFirst("font.total");
                        System.out.println("产品组: " + cpz.text() + "; 产品: " + total.text());


                        brandInfo.setProductnum(Integer.parseInt(cpz.text()));

                        int pageCur = 0;
                        int pageTotal = 0;
                        Elements labels = document.select("div.pagination > label");
                        if ( labels.size()==2 ) {
                            pageCur = Integer.parseInt(labels.get(0).text());
                            pageTotal = Integer.parseInt(labels.get(1).text());
                        }

                        System.out.println("当前页: " + pageCur);
                        System.out.println("总页数: " + pageTotal);

                        brandInfo.setPagenum(pageTotal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                   SQLExecutor.update(brandInfo);
                } catch (Exception e) {

                } finally {
                    System.out.print("\r" + i  + "/" + brandInfoList.size() + ": " + brandInfo.getName1());
                }


            }
        }

    }

    private static void loadBrandInfo() throws Exception {
        HtmlCache html = HtmlPageLoader.getInstance().loadHtmlPage("https://www.grainger.cn/c-000001.html", false);
        if ( html==null || StringUtils.isBlank(html.getHtml())) {
            System.out.println("Html is blank.");
            return;
        }

        Document doc = Jsoup.parse(html.getHtml());


        Element allbrand = doc.selectFirst("div.allbrand");
        allbrand.remove();

        Elements brands = allbrand.select("dd a");
        int total1 = 0;
        for (Element brand : brands) {
            System.out.println(brand.text());
            total1 += Integer.parseInt(getNumber(brand.text()));

            String href = brand.attr("href");


            Info info = parse(brand.text());

            BrandInfo brandInfo = new BrandInfo();
            brandInfo.setCode(getBrandCode(href));
            brandInfo.setUrl("https://www.grainger.cn" + href);
            brandInfo.setName1(info.name1);
            brandInfo.setName2(info.name2);
            brandInfo.setSkunum(info.count);

            SQLExecutor.insert(brandInfo);

        }

        System.out.println(brands.size());


        Element li_m = doc.selectFirst("div.li_m");
        li_m.remove();

        Elements cates = li_m.select("dd a");
        int total = 0;
        for (Element brand : cates) {
            System.out.println(brand.text() + " -- " + getNumber(brand.text()));
            total += Integer.parseInt(getNumber(brand.text()));


        }

        System.out.println(cates.size() + " ==> " + total + "; " + total1);
    }
}
