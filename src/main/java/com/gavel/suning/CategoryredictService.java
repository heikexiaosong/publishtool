package com.gavel.suning;

import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.entity.custom.CategoryredictGetRequest;
import com.suning.api.entity.custom.CategoryredictGetResponse;
import com.suning.api.exception.SuningApiException;

public class CategoryredictService {

    public static CategoryredictService buildService(String serverUrl, String appKey, String appSecret) {
        return new CategoryredictService(new DefaultSuningClient(serverUrl, appKey, appSecret));
    }

    private CategoryredictService(DefaultSuningClient client) {
        this.client = client;
    }

    private final DefaultSuningClient client;

    public String redict(String cmtitle, String categoryCode) {

        CategoryredictGetRequest shopInfo = new CategoryredictGetRequest();
        shopInfo.setCmTitle(cmtitle);
        shopInfo.setCategoryCode(categoryCode);

        try {
            CategoryredictGetResponse response = client.excute(shopInfo);
            if ( response.getSnerror()!=null ) {
                System.out.println(new Gson().toJson(response.getSnerror()));
                return "";
            } else {
                CategoryredictGetResponse.GetCategoryredict categoryredict = response.getSnbody().getGetCategoryredict();
                System.out.println(categoryredict.getCmTitle());
                System.out.println(categoryredict.getCatRight());

                System.out.println(new Gson().toJson(categoryredict.getCategoryList()));
                if ( categoryredict.getCategoryList()!=null && categoryredict.getCategoryList().size()>0 ) {
                    String est = categoryredict.getCategoryList().get(0).getCatIdEst();
                    return est.substring(est.lastIndexOf("#") + 1);
                }

                if ( "Y".equalsIgnoreCase(categoryredict.getCatRight()) ) {
                    return categoryCode;
                }
            }
        } catch (SuningApiException e) {
            e.printStackTrace();
        }


        return "";
    }
}
