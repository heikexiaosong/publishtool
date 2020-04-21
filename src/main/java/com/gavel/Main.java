package com.gavel;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Itemparameter;
import com.gavel.suning.SuningClient;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SuningResponse;
import com.suning.api.entity.selfmarket.ApplyAddRequest;
import com.suning.api.entity.selfmarket.ApplyAddResponse;
import com.suning.api.exception.SuningApiException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Main {


    private  static  final DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");

    public static HtmlCache load(String url, String category) throws Exception {

        if ( url==null || url.trim().length() <= 0 ) {
            return null;
        }

        String content = HttpUtils.get(url.trim(), "https://www.grainger.cn/c-" + category + ".html");
        if ( content==null || content.trim().length()==0 ) {
            return null;
        }

        HtmlCache cache = new HtmlCache();
        cache.setUrl(url.trim());
        cache.setHtml(content);
        cache.setContentlen(content.length());
        cache.setUpdatetime(Calendar.getInstance().getTime());
        return cache;

    }

    public static void main(String[] args) throws Exception {

        String code = "5P9573";

        String url = "https://www.grainger.cn/u-" + code + ".html";


        HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = '" + url + "' limit 1 ", HtmlCache.class);
        if ( cache == null ) {
            cache = load(url, "207079");
        }

        if ( cache==null || cache.getHtml()==null || cache.getHtml().trim().length()<=0 ) {
            System.out.println("exit");
            return;
        }

        Document doc = Jsoup.parse(cache.getHtml());

        Element proDetailCon = doc.selectFirst("div.proDetailCon");

        System.out.println(proDetailCon.selectFirst("h3 > a").html());

        System.out.println(proDetailCon.select("div font").size());

        for (Element div : proDetailCon.select("div font")) {
            System.out.println(div.text());
        }



        Elements fonts = proDetailCon.select("div font");

        String name = proDetailCon.selectFirst("h3 > a").text();
        String model = fonts.get(2).text();


        StringBuilder title = new StringBuilder();
        title.append(fonts.get(1).text()).append(" ");
        if ( !name.contains(model.trim()) ) {
            title.append(model).append(" ");
        }
        title.append(name).append(" ");
        title.append(fonts.get(3).text());



        System.out.println(title);

        String sellingPoint = proDetailCon.selectFirst("h4  span").text();
        System.out.println(sellingPoint);


        Element tableDiv = doc.selectFirst("div.tableDiv");
        //System.out.println(tableDiv.text());

        Element leftTable2 = tableDiv.selectFirst("div.leftTable2");

        for (Element trsku2 : leftTable2.select("tr.trsku2")) {

            System.out.println("订货号： " +  trsku2.child(0).attr("title"));
            System.out.println("制造商型号： " +  trsku2.child(1).attr("title"));

        }

        Element rightTable1 = tableDiv.selectFirst("div#rightTable1");

        List<String> columnName = new ArrayList<>();
        Element pxTR = rightTable1.selectFirst("tr.pxTR");
        for (Element element : pxTR.children()) {
            columnName.add(element.attr("title"));
        }

        Element rightTable2 = tableDiv.selectFirst("div#rightTable2");
        Elements trskus = rightTable2.select("tr.trsku2");

        for (Element trsku : trskus) {

            Elements tds = trsku.children();


            for (int i = 0; i < tds.size(); i++) {
                Element td = tds.get(i);
                System.out.println(columnName.get(i) + ": " + td.attr("title"));
            }

            System.out.println("================");
        }


        Element img = doc.selectFirst("div.box > img");
        System.out.println(img.attr("src"));






        String category = "R9002908";
        List<Itemparameter> itemparameters =  SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER  where CATEGORYCODE = '" + category + "' ", Itemparameter.class);
        for (Itemparameter parameter : itemparameters) {
            System.out.println(new Gson().toJson(parameter));
            if ( parameter.getIsMust() !=null && "X".equalsIgnoreCase(parameter.getIsMust()) ) {
                System.out.println( "[" + parameter.getIsMust() + "]" + parameter.getParCode() + ": " + parameter.getParName() + " ==> " + parameter.getParType() + " >>>  " + parameter.getDataType());

                System.out.println("\t" + parameter.getOptions());
                if (  parameter.getParOption()!=null &&  parameter.getParOption().size() > 0 ) {
                    for (Itemparameter.ParOption parOption : parameter.getParOption()) {
                        System.out.println("\t" + parOption.getParOptionCode() +  ": " +  parOption.getParOptionDesc());
                    }
                }
            }
        }

        if ( 1==1 ) {
            return;
        }



        ApplyAddRequest request = new ApplyAddRequest();



        // https://www.grainger.cn/u-3C3377.html
        request.setCategoryCode(category);  // 类目编码
        request.setBrandCode("Y621");       // 品牌编码

        String suffix = "-测试";

        request.setItemCode(code + suffix); // 供应商商品编码
        request.setProductName(title + suffix);  // 大衣	商品名称
        request.setCmTitle(title + suffix); // 商品标题

        request.setSellingPoints(sellingPoint); // 商品卖点

        /**
         * 商家商品介绍，UTF-8格式。将html内容的txt文本文件读取为字节数组,然后base64加密，去除空格回车后作为字段，传输时所涉及的图片不得使用外部url。允许写入CSS（禁止引用外部CSS）不支持JS。
         */
        request.setIntroduction("5aW95ZWG5ZOB"); // 商家商品介绍 -- 好品质好商品


        List<ApplyAddRequest.Pars> parsList =new ArrayList<ApplyAddRequest.Pars>();


        for (Itemparameter parameter : itemparameters) {
            if ( parameter.getIsMust() !=null && "X".equalsIgnoreCase(parameter.getIsMust()) ) {
                ApplyAddRequest.Pars pars= new ApplyAddRequest.Pars();
                pars.setParCode(parameter.getParCode());

                if (  parameter.getParOption()!=null &&  parameter.getParOption().size() > 0 ) {
                    pars.setParValue(parameter.getParOption().get(0).getParOptionCode());
                }

                if (  "3".equalsIgnoreCase(parameter.getParType())  ) {
                    pars.setParValue("1");
                }


                switch ( parameter.getParCode() ) {
                    case "cmModel": // 商品型号
                        pars.setParValue(model);
                        break;
                    case "country": // 国家
                        pars.setParValue("cn");
                        break;
                    case "taxCateg": // 商品税分类
                        pars.setParValue("E"); // JE--13% 进项税，中国
                        break;
                    case "G0000": // 色卡
                        pars.setParValue(""); // JE--13% 进项税，中国
                        break;
                    case "G00001": // 颜色
                        pars.setParValue(""); // JE--13% 进项税，中国
                        break;
                }
                parsList.add(pars);
            }
        }

        request.setPars(parsList);


        /**
         * packingList	String	N
         * packingListName	String	N	电脑	    装箱清单名单
         * packingListQty	String	N	1	    装箱清单名单数量
         */
        List<ApplyAddRequest.PackingList> packingList = new ArrayList<>();
        ApplyAddRequest.PackingList packingList1 = new ApplyAddRequest.PackingList();
        packingList1.setPackingListName("主机");
        packingList1.setPackingListQty("1");
        packingList.add(packingList1);

        request.setPackingList(packingList);


        // supplierImgUrl 商家商品图片 urlA~urlE
//        List<ApplyAddRequest.SupplierImgUrl> supplierImgUrls = new ArrayList<>();
//        ApplyAddRequest.SupplierImgUrl supplierImgUrl = new ApplyAddRequest.SupplierImgUrl();
//        supplierImgUrl.setUrlA("https://imgservice.suning.cn/uimg1/b2c/image/5V9eSsvaZdo5ONrqPJRIww.jpg_800w_800h");
//        request.setSupplierImgUrl(supplierImgUrls);

        //request.setSupplierImg1Url("https://static.grainger.cn/product_images_new/800/2A1/2013121616015256969.JPG");

        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        try {
            ApplyAddResponse response = client.excute(request);
            System.out.println("ApplyAddRequest :" + response.getBody());

            SuningResponse.SnError error = response.getSnerror();
            if ( error!=null ) {
                System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
            } else {
                System.out.println(new Gson().toJson(response.getSnbody().getAddApply()));
            }



        } catch (SuningApiException e) {
            e.printStackTrace();
        }



    }
}
