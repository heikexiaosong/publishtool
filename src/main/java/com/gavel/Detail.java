package com.gavel;

import com.gavel.config.APPConfig;
import com.google.gson.Gson;
import com.suning.api.SuningResponse;
import com.suning.api.entity.selfmarket.ItemQueryRequest;
import com.suning.api.entity.selfmarket.ItemQueryResponse;
import com.suning.api.entity.selfmarket.ItemdetailQueryRequest;
import com.suning.api.entity.selfmarket.ItemdetailQueryResponse;
import com.suning.api.exception.SuningApiException;

public class Detail {


    public static void main(String[] args) {

        {
            ItemQueryRequest request1 = new ItemQueryRequest();
            request1.setPageNo(1);
            request1.setPageSize(50);
            request1.setCategoryCode("R1309004");
            try {
                ItemQueryResponse response = APPConfig.getInstance().client().excute(request1);
                System.out.println("ApplyAddRequest :" + response.getBody());
                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    for (ItemQueryResponse.QueryItem queryItem : response.getSnbody().getQueryItem()) {

                        System.out.println(new Gson().toJson(queryItem));
                    }
                }
            } catch (SuningApiException e) {
                e.printStackTrace();
            }
        }

        {
            ItemdetailQueryRequest request1 = new ItemdetailQueryRequest();
            request1.setApplyCode("7043e079-ce8f-4692-b141-33ec066572eb");
            try {
                ItemdetailQueryResponse response = APPConfig.getInstance().client().excute(request1);
                System.out.println("ApplyAddRequest :" + response.getBody());
                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    System.out.println(new Gson().toJson(response.getSnbody().getQueryItemdetail()));
                }
            } catch (SuningApiException e) {
                e.printStackTrace();
            }
        }

    }
}
