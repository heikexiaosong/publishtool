package com.gavel;

import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Brand;
import com.gavel.entity.Category;
import com.gavel.entity.Shopinfo;
import com.google.gson.Gson;
import com.suning.api.SelectSuningResponse;
import com.suning.api.entity.item.BrandQueryRequest;
import com.suning.api.entity.item.BrandQueryResponse;
import com.suning.api.exception.SuningApiException;

import java.util.List;

public class BrandLoad {

    public static void main(String[] args) throws Exception {

        Shopinfo shopinfo = APPConfig.getInstance().getShopinfo();

        System.out.println(shopinfo.getName());


        List<Category> cates = SQLExecutor.executeQueryBeanList("select * from  CATEGORY where SUPPLIERCODE = ? and  CATEGORYCODE = ? ", Category.class, shopinfo.getCode(), "R9008752");
        System.out.println("Cate: " +cates.size());

        int i = 0;
        for (Category cate : cates) {
            System.out.println("[" +(i++) + "/" + cates.size()  + "] " + cate.getCategoryCode() + " - " + cate.getCategoryName());
            queryBrand(cate.getCategoryCode(), shopinfo.getCode());
        }




    }

    private static void queryBrand(String categoryCode, String shopid) throws Exception {

        int pageNo = 1;

        BrandQueryRequest request = new BrandQueryRequest();
        request.setPageSize(50);
        request.setPageNo(pageNo);
        request.setCategoryCode(categoryCode);
//        request.setCategoryName("家装建材及五金");
        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        try {
            BrandQueryResponse response = APPConfig.getInstance().client().excute(request);
            System.out.println("CategoryQueryRequest :" + response.getBody());

            SelectSuningResponse.SnHead head = response.getSnhead();

            while ( response.getSnbody()!=null && response.getSnbody().getBrandQueries()!=null && response.getSnbody().getBrandQueries().size() == 50 ) {
                for (BrandQueryResponse.BrandQuery brandQuery : response.getSnbody().getBrandQueries()) {
                    System.out.println(new Gson().toJson(brandQuery));

                    //新增

                    Brand brand = new Brand();

                    brand.setSupplierCode(shopid);
                    brand.setCategoryCode(categoryCode);
                    brand.setCode(brandQuery.getBrandCode());
                    brand.setName(brandQuery.getBrandName());
                    try {
                        SQLExecutor.insert(brand);
                    } catch (Exception e) {
                        System.out.println(categoryCode + "-" + brandQuery.getBrandCode() + "： " + e.getMessage());
                    }

                }

                pageNo += 1;
                request.setPageNo(pageNo);
                response = APPConfig.getInstance().client().excute(request);
            }

            if (response.getSnbody()!=null && response.getSnbody().getBrandQueries()!=null && response.getSnbody().getBrandQueries().size() > 0 ) {
                for (BrandQueryResponse.BrandQuery brandQuery : response.getSnbody().getBrandQueries()) {
                    System.out.println(new Gson().toJson(brandQuery));
                    Brand brand = new Brand();

                    brand.setSupplierCode(shopid);
                    brand.setCategoryCode(categoryCode);
                    brand.setCode(brandQuery.getBrandCode());
                    brand.setName(brandQuery.getBrandName());
                    try {
                        SQLExecutor.insert(brand);
                    } catch (Exception e) {
                        System.out.println(categoryCode + "-" + brandQuery.getBrandCode() + "： " + e.getMessage());
                    }
                }

            }



        } catch (SuningApiException e) {
            e.printStackTrace();
        }


    }
}
