package com.gavel.grainger;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import com.gavel.entity.Itemparameter;
import com.gavel.suning.SuningClient;
import com.gavel.utils.StringUtils;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.ItemparametersQueryRequest;
import com.suning.api.entity.item.ItemparametersQueryResponse;
import com.suning.api.exception.SuningApiException;

import java.util.ArrayList;
import java.util.List;

public class ItemparamterLoad {

    private  static  final DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");

    public static void main(String[] args) throws Exception {

        List<Category>  categories =  SQLExecutor.executeQueryBeanList("select * from CATEGORY", Category.class);

        for (Category category : categories) {

            loadItemparameters(category.getCategoryCode());
        }

    }

    private static List<Itemparameter> loadItemparameters(String categoryCode) {

        List<Itemparameter> itemparameters = new ArrayList<>();
        if ( categoryCode==null || categoryCode.trim().length()<=0 ) {
            return itemparameters;
        }

        ItemparametersQueryRequest request = new ItemparametersQueryRequest();
        request.setCategoryCode(categoryCode);


        int next = 1;
        int total = 1;

        while ( next <= total ) {
            request.setPageNo(next);
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

                    next = response.getSnhead().getPageNo() + 1;
                    total = response.getSnhead().getPageTotal();

                    for (ItemparametersQueryResponse.ItemparametersQuery item : response.getSnbody().getItemparametersQueries()) {

                        Itemparameter itemparameter = new Itemparameter();

                        itemparameter.setCategoryCode(categoryCode.trim());
                        itemparameter.setParaTemplateCode(item.getParaTemplateCode());
                        itemparameter.setParaTemplateDesc(item.getParaTemplateDesc());
                        itemparameter.setParCode(item.getParCode());
                        itemparameter.setParName(item.getParName());
                        itemparameter.setParType(item.getParType());
                        itemparameter.setParUnit(item.getParUnit());
                        itemparameter.setDataType(item.getDataType());
                        itemparameter.setIsMust(item.getIsMust());
                        itemparameter.setOptions(new Gson().toJson(item.getParOption()));

                        System.out.println(new Gson().toJson(item));

                        if ( item.getParOption()!=null &&  item.getParOption().size() == 1 ) {
                            ItemparametersQueryResponse.ParOption option = item.getParOption().get(0);
                            itemparameter.setParam(option.getParOptionCode());
                            if (StringUtils.isBlank(option.getParOptionCode())) {
                                itemparameter.setParam(option.getParOptionDesc());
                            }
                        }


                        try {
                            SQLExecutor.insert(itemparameter);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }



            } catch (SuningApiException e) {
                e.printStackTrace();
            }

        }



        return itemparameters;
    }
}
