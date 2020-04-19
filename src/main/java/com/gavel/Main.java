package com.gavel;

import com.gavel.suning.SuningClient;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SuningResponse;
import com.suning.api.entity.selfmarket.ApplyAddRequest;
import com.suning.api.entity.selfmarket.ApplyAddResponse;
import com.suning.api.exception.SuningApiException;

import java.util.ArrayList;
import java.util.List;

public class Main {


    private  static  final DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");

    public static void main(String[] args) {

        ApplyAddRequest request = new ApplyAddRequest();

        // https://www.grainger.cn/u-3C3377.html
        request.setCategoryCode("R9002727");  // 类目编码
        request.setBrandCode("8653");       // 品牌编码
        request.setCmTitle("Honeywell FUS30S-HP FUSION 小号 可重复使用 圣诞树型 硅胶耳-测试"); // 商品标题
        request.setSellingPoints("Honeywell 圣诞树型带线耳塞"); // 商品卖点

        /**
         * 商家商品介绍，UTF-8格式。将html内容的txt文本文件读取为字节数组,然后base64加密，去除空格回车后作为字段，传输时所涉及的图片不得使用外部url。允许写入CSS（禁止引用外部CSS）不支持JS。
         */
        request.setIntroduction("5aW95ZWG5ZOB"); // 商家商品介绍 -- 好品质好商品
        request.setItemCode("3C3377"); // 供应商商品编码

        request.setProductName("霍尼韦尔 Honeywell 圣诞树型带线耳塞");  // 大衣	商品名称

        List<ApplyAddRequest.Pars> parsList =new ArrayList<ApplyAddRequest.Pars>();

        ApplyAddRequest.Pars pars= new ApplyAddRequest.Pars();
        pars.setParCode("cmModel");
        pars.setParValue("cn");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("LAENG");
        pars.setParValue("440");
        parsList.add(pars);


        pars= new ApplyAddRequest.Pars();
        pars.setParCode("BREIT");
        pars.setParValue("200");
        parsList.add(pars);


        pars= new ApplyAddRequest.Pars();
        pars.setParCode("HOEHE");
        pars.setParValue("100");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("VOLUM");
        pars.setParValue("430");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("BRGEW");
        pars.setParValue("0.041");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("CNT");
        pars.setParValue("10");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("country");
        pars.setParValue("cn");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("taxCateg");
        pars.setParValue("E");
        parsList.add(pars);


        pars= new ApplyAddRequest.Pars();
        pars.setParCode("005736");
        pars.setParValue("01");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("005735");
        pars.setParValue("14");
        parsList.add(pars);


        pars= new ApplyAddRequest.Pars();
        pars.setParCode("005734");
        pars.setParValue("04");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("005733");
        pars.setParValue("03");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("000202");
        pars.setParValue("其他");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("007503");
        pars.setParValue("32");
        parsList.add(pars);

        pars= new ApplyAddRequest.Pars();
        pars.setParCode("000136");
        pars.setParValue("其他");
        parsList.add(pars);


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
