package com.gavel.suning;

import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import com.gavel.entity.Itemparameter;
import com.gavel.entity.Shopinfo;
import com.gavel.utils.StringUtils;
import com.google.gson.Gson;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.ItemparametersQueryRequest;
import com.suning.api.entity.item.ItemparametersQueryResponse;
import com.suning.api.exception.SuningApiException;

import java.util.ArrayList;
import java.util.List;

public class ItemparamterLoad {

    public static void main(String[] args) throws Exception {

        Shopinfo shopinfo = APPConfig.getInstance().getShopinfo();

        System.out.println(shopinfo.getName());


        List<Category> categories = SQLExecutor.executeQueryBeanList("select * from  CATEGORY where SUPPLIERCODE = ? and  CATEGORYCODE = ? ", Category.class, shopinfo.getCode(), "R9009826");
        System.out.println("Cate: " + categories.size());

        for (Category category : categories) {

            loadItemparameters(category.getCategoryCode(), shopinfo.getCode());
        }

    }

    public static List<Itemparameter> loadItemparameters(String categoryCode, String shopid) {

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
                ItemparametersQueryResponse response = APPConfig.getInstance().client().excute(request);
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
                        itemparameter.setSupplierCode(shopid);
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
                        } finally {
                            itemparameters.add(itemparameter);
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
