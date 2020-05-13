package com.gavel.suning;

import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.entity.selfmarket.PurchaseOrderQueryRequest;
import com.suning.api.entity.selfmarket.PurchaseOrderQueryResponse;
import com.suning.api.exception.SuningApiException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OrderLoad {

    private  static  final DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");

    public static void main(String[] args) throws Exception {

        PurchaseOrderQueryRequest request = new PurchaseOrderQueryRequest();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");


        Calendar calendar = Calendar.getInstance();



        Date end = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date start = calendar.getTime();

        for (int i = 0; i < 100; i++) {
            System.out.println("The Date: " + simpleDateFormat.format(start) + " - " + simpleDateFormat.format(end));


            // request.setOrderCode("4500075418");
            request.setStartDate( simpleDateFormat.format(start));
            request.setEndDate(simpleDateFormat.format(end));

            end = start;
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            start = calendar.getTime();

            request.setOrderType("NB");
            request.setOrderStatus("10");
            request.setPageNo(1);
            request.setPageSize(50);
//api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
            request.setCheckParam(true);

            try {
                PurchaseOrderQueryResponse response = client.excute(request);
                System.out.println("返回json/xml格式数据 :" + response.getBody());

                if ( response.getSnerror()!=null ) {
                    System.out.println(new Gson().toJson(response.getSnerror()));

                    continue;
                }

                System.out.println(new Gson().toJson(response.getSnhead()));


                for (PurchaseOrderQueryResponse.PurchaseOrder purchaseOrder : response.getSnbody().getPurchaseOrder()) {
                    System.out.println(new Gson().toJson(purchaseOrder));
                }

            } catch (SuningApiException e) {
                e.printStackTrace();
            }




        }




    }
}
