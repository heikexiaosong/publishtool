package com.gavel.suning;

import com.suning.api.entity.custom.CategoryredictGetRequest;

public class Categoryredict {


    public static void main(String[] args) throws Exception {

        {
            CategoryredictGetRequest shopInfo = new CategoryredictGetRequest();
            shopInfo.setCmTitle("3M AC215A1 安全绳，直径为16mm(包装数量 1个) 99");
            shopInfo.setCategoryCode("R9002859");


        }


        CategoryredictService categoryredictService = null; // CategoryredictService.buildService(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET);

        System.out.println(categoryredictService.redict("爱柯部落 ECOBOOTHS 122CM×1000CM×12MM 舒柯防震地垫", "R9002887"));

    }

}
