package com.gavel;

import com.gavel.suning.SuningClient;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.ItemparametersQueryRequest;
import com.suning.api.entity.item.ItemparametersQueryResponse;
import com.suning.api.entity.master.CityQueryRequest;
import com.suning.api.entity.master.CityQueryResponse;
import com.suning.api.entity.selfmarket.*;
import com.suning.api.exception.SuningApiException;

import java.util.ArrayList;
import java.util.List;

public class Main {



    public static void main(String[] args) {
        System.out.println("Hello");
        String category = "R9014610";

        DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");

        {
            CityQueryRequest request = new CityQueryRequest();
            request.setNationCode("zn");

            //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
            request.setCheckParam(true);


            try {
                CityQueryResponse response = client.excute(request);
                System.out.println("NationQueryRequest :" + response.getBody());

                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    for (CityQueryResponse.City nation : response.getSnbody().getCity()) {
                        System.out.println(new Gson().toJson(nation));
                    }
                }



            } catch (SuningApiException e) {
                e.printStackTrace();
            }
        }

        {


            ItemparametersQueryRequest request = new ItemparametersQueryRequest();
            request.setCategoryCode(category);
            request.setPageNo(1);
            request.setPageSize(20);
            //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
            request.setCheckParam(true);


            try {
                ItemparametersQueryResponse response = client.excute(request);
                System.out.println("NationQueryRequest :" + response.getBody());

                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    for (ItemparametersQueryResponse.ItemparametersQuery itemparameters : response.getSnbody().getItemparametersQueries()) {
                        System.out.println(new Gson().toJson(itemparameters));
                    }
                }



            } catch (SuningApiException e) {
                e.printStackTrace();
            }
        }


        if ( 1==2 ) {


            ApplyAddRequest request = new ApplyAddRequest();

            request.setCategoryCode(category);
            request.setBrandCode("1G40");
            request.setProductName("API测试商品");
            //request.setItemCode("T91781242012");
            request.setItemCode("T91781242012");
            request.setIntroduction("5aW95ZWG5ZOB");
            request.setCmTitle("API测试商品标题");

            request.setSellingPoints("宇宙第二");
            //request.setBarpic("http://static.grainger.cn/product_images_new/350/brand/2727_MHC(%E5%BD%A9%E8%89%B2).jpg");

//            request.setSellPoint("宇宙最火");
//            request.setFreightTemplateId("2da499d40cda4389817273ab0affdbed");
//            request.setPrice("7999.98");
//            request.setInvQty("546");
//            request.setAlertQty("100");
//            request.setAfterSaleServiceDec("7天内包退换");
//            request.setSaleSet("0");

            List<ApplyAddRequest.Pars> parsList =new ArrayList<ApplyAddRequest.Pars>();

            ApplyAddRequest.Pars pars= new ApplyAddRequest.Pars();
            pars.setParCode("LAENG");
            pars.setParValue("440");
            parsList.add(pars);

            pars= new ApplyAddRequest.Pars();
            pars.setParCode("cmModel");
            pars.setParValue("CN");
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
            pars.setParCode("001360");
            pars.setParValue("WSE");
            parsList.add(pars);

            pars= new ApplyAddRequest.Pars();
            pars.setParCode("000136");
            pars.setParValue("其他");
            parsList.add(pars);


            request.setPars(parsList);


            List<ApplyAddRequest.PackingList> packingList = new ArrayList<>();
            ApplyAddRequest.PackingList packingList1 = new ApplyAddRequest.PackingList();
            packingList1.setPackingListName("主机");
            packingList1.setPackingListQty("1");
            packingList.add(packingList1);

            request.setPackingList(packingList);

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
}
