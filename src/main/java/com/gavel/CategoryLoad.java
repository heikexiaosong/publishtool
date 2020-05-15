package com.gavel;

import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import com.gavel.entity.Shopinfo;
import com.google.gson.Gson;
import com.suning.api.entity.item.CategoryQueryRequest;
import com.suning.api.entity.item.CategoryQueryResponse;
import com.suning.api.exception.SuningApiException;

public class CategoryLoad {

    public static void main(String[] args) throws Exception {

        Shopinfo shopinfo = APPConfig.getInstance().getShopinfo();

        System.out.println(shopinfo.getName());


        int pageNo = 1;

        CategoryQueryRequest request = new CategoryQueryRequest();
        request.setPageSize(50);
        request.setPageNo(pageNo);
//        request.setCategoryName("家装建材及五金");
        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        try {
            CategoryQueryResponse response = APPConfig.getInstance().client().excute(request);
            System.out.println("CategoryQueryRequest :" + response.getBody());

            while ( response.getSnbody()!=null && response.getSnbody().getCategoryQueries()!=null && response.getSnbody().getCategoryQueries().size() == 50 ) {
                for (CategoryQueryResponse.CategoryQuery categoryQuery : response.getSnbody().getCategoryQueries()) {
                    System.out.println(new Gson().toJson(categoryQuery));

                    Category category = new Category();

                    category.setSupplierCode(shopinfo.getCode());
                    category.setCategoryCode(categoryQuery.getCategoryCode());
                    category.setCategoryName(categoryQuery.getCategoryName());
                    category.setIsBottom(categoryQuery.getIsBottom());
                    category.setDescPath(categoryQuery.getDescPath());
                    category.setGrade(categoryQuery.getGrade());

                    try {
                        SQLExecutor.insert(category);
                    } catch (Exception e) {
                        System.out.println(categoryQuery.getCategoryCode() + "： " + e.getMessage());
                    }

                }


                pageNo += 1;
                request.setPageNo(pageNo);
                response = APPConfig.getInstance().client().excute(request);
            }

            if ( response.getSnbody()!=null && response.getSnbody().getCategoryQueries()!=null && response.getSnbody().getCategoryQueries().size() > 0 ) {
                for (CategoryQueryResponse.CategoryQuery categoryQuery : response.getSnbody().getCategoryQueries()) {
                    System.out.println(new Gson().toJson(categoryQuery));

                    //新增

                    Category category = new Category();

                    category.setSupplierCode(shopinfo.getCode());
                    category.setCategoryCode(categoryQuery.getCategoryCode());
                    category.setCategoryName(categoryQuery.getCategoryName());
                    category.setIsBottom(categoryQuery.getIsBottom());
                    category.setDescPath(categoryQuery.getDescPath());
                    category.setGrade(categoryQuery.getGrade());

                    try {
                        SQLExecutor.insert(category);
                    } catch (Exception e) {
                        System.out.println(categoryQuery.getCategoryCode() + "： " + e.getMessage());
                    }
                }
            }




        } catch (SuningApiException e) {
            e.printStackTrace();
        }

    }
}
