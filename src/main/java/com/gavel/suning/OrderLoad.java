package com.gavel.suning;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SelectSuningResponse;
import com.suning.api.entity.selfmarket.OrdercodeQueryRequest;
import com.suning.api.entity.selfmarket.SaleOrderQueryRequest;
import com.suning.api.entity.selfmarket.SaleOrderQueryResponse;
import com.suning.api.entity.shop.ShopInfoGetRequest;
import com.suning.api.entity.shop.ShopInfoGetResponse;
import com.suning.api.exception.SuningApiException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class OrderLoad {

    private  static  final DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, "7e8ab1a1444856e80c7a79650c3022cb", "48b64e40dcef582abfc4555b10ace5df", "json");

    public static void main(String[] args) throws Exception {

        {
            ShopInfoGetRequest shopInfo = new ShopInfoGetRequest();

            try {
                ShopInfoGetResponse response = client.excute(shopInfo);
                System.out.println("返回json/xml格式数据 :" + response.getBody());

                if ( response.getSnerror()!=null ) {
                    System.out.println(new Gson().toJson(response.getSnerror()));
                } else {
                    System.out.println(new Gson().toJson(response.getSnhead()));


                    System.out.println(new Gson().toJson(response.getSnbody().getGetShopInfo()));

                }


            } catch (SuningApiException e) {
                e.printStackTrace();
            }
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        {
            OrdercodeQueryRequest request = new OrdercodeQueryRequest();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);


            Date end = calendar.getTime();

            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date start = calendar.getTime();


            Set<String> codeSet = new HashSet<>();

            for (int i = 0; i < 1; i++) {
                System.out.println("The Date: " + simpleDateFormat.format(start) + " - " + simpleDateFormat.format(end));


                try {

                    loadOrder(start, end);

                } catch (Exception e) {

                }


            }

            System.out.println("Order: " + codeSet.size());


        }




        {
            SaleOrderQueryRequest request = new SaleOrderQueryRequest();





            Calendar calendar = Calendar.getInstance();



            Date end = calendar.getTime();

            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date start = calendar.getTime();

            for (int i = 0; i < 1; i++) {
                System.out.println("The Date: " + simpleDateFormat.format(start) + " - " + simpleDateFormat.format(end));


                // request.setOrderCode("4500075418");
                request.setStartTime( simpleDateFormat.format(start));
                request.setEndTime(simpleDateFormat.format(end));

                end = start;
                calendar.add(Calendar.DAY_OF_MONTH, -30);
                start = calendar.getTime();


                request.setSupplierCode("10148425");
//
//            request.setOrderType("NB");
//            request.setOrderStatus("10");
                request.setPageNo(1);
                request.setPageSize(50);
//api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
                request.setCheckParam(true);

                try {
                    SaleOrderQueryResponse response = client.excute(request);
                    System.out.println("返回json/xml格式数据 :" + response.getBody());

                    if ( response.getSnerror()!=null ) {
                        System.out.println(new Gson().toJson(response.getSnerror()));

                        continue;
                    }

                    System.out.println(new Gson().toJson(response.getSnhead()));


                    for (SaleOrderQueryResponse.QuerySaleOrder purchaseOrder : response.getSnbody().getQuerySaleOrder()) {
                        System.out.println(new Gson().toJson(purchaseOrder));
                    }

                } catch (SuningApiException e) {
                    e.printStackTrace();
                }




            }
        }
    }


    private  static int  loadOrder(Date start, Date end) throws FileNotFoundException {



        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        BufferedWriter writer = Files.newWriter(new File(dateFormat.format(start) + "-" + dateFormat.format(end)), Charset.forName("UTF8"));


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int nextPageNo = 1;
        int totalPageNo = 1;

        int total = 0;


        SaleOrderQueryRequest request = new SaleOrderQueryRequest();
        request.setSupplierCode("10148425");
        request.setPageSize(100);


        while ( nextPageNo <= totalPageNo ) {
            request.setPageNo(nextPageNo);
            request.setStartTime( simpleDateFormat.format(start));
            request.setEndTime(simpleDateFormat.format(end));

            request.setCheckParam(true);

            try {
                SaleOrderQueryResponse response = client.excute(request);
                System.out.println("返回json/xml格式数据 :" + response.getBody());

                if ( response.getSnerror()!=null ) {
                    System.out.println(new Gson().toJson(response.getSnerror()));

                    return -1;
                }

                SelectSuningResponse.SnHead head = response.getSnhead();
                nextPageNo = head.getPageNo() + 1;
                totalPageNo = head.getPageTotal();

                total = Integer.parseInt(head.getTotalSize());

                for (SaleOrderQueryResponse.QuerySaleOrder saleOrder : response.getSnbody().getQuerySaleOrder()) {
                    try {
                        writer.write(new Gson().toJson(saleOrder));
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println(new Gson().toJson(saleOrder));
                    }
                }

            } catch (SuningApiException e) {
                e.printStackTrace();
            }
        }

        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return total;

    }

}
