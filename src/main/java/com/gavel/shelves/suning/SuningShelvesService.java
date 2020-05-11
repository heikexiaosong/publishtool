package com.gavel.shelves.suning;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.ShelvesItem;
import com.gavel.shelves.ParameterLoader;
import com.gavel.shelves.ShelvesService;
import com.gavel.suning.SuningClient;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SuningResponse;
import com.suning.api.entity.selfmarket.ApplyAddRequest;
import com.suning.api.entity.selfmarket.ApplyAddResponse;
import com.suning.api.exception.SuningApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SuningShelvesService implements ShelvesService {

    private  static  final DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");

    private Logger logger = LoggerFactory.getLogger(SuningShelvesService.class);

    @Override
    public void shelves(ShelvesItem item) throws Exception {

        if ( item==null ) {
           throw new Exception("Item 不能为空");
        }

        if ( item.getMappingcategorycode()==null || item.getMappingcategorycode().trim().length()==0 ) {
            throw new Exception("[Item: " + item.getItemCode() + "]上架类目没有设置");
        }

        if ( item.getMappingbrandcode()==null || item.getMappingbrandcode().trim().length()==0 ) {
            throw new Exception("[Item: " + item.getItemCode() + "]上架品牌没有设置");
        }

        String category = item.getMappingcategorycode().trim();
        String brand = item.getMappingbrandcode().trim();

        ApplyAddRequest request = new ApplyAddRequest();

        request.setCategoryCode(category);  // 类目编码
        request.setBrandCode(brand);        // 品牌编码
        request.setItemCode(item.getItemCode()); // 供应商商品编码
        request.setProductName(item.getProductName());
        request.setCmTitle(item.getCmTitle());         // 商品标题
        request.setSellingPoints(item.getSellingPoints()); // 商品卖点

        /**
         * 商家商品介绍，UTF-8格式。将html内容的txt文本文件读取为字节数组,然后base64加密，去除空格回车后作为字段，传输时所涉及的图片不得使用外部url。允许写入CSS（禁止引用外部CSS）不支持JS。
         */
        request.setIntroduction(item.getIntroduction()); // 商品介绍

        // 商品属性设置
        ParameterLoader parameterLoader = new SuningParameterLoader();
        List<ParameterLoader.Parameter> parameters = parameterLoader.loadParameters(category);

        List<ApplyAddRequest.Pars> parsList =new ArrayList<ApplyAddRequest.Pars>();
        for (ParameterLoader.Parameter parameter : parameters) {
            ApplyAddRequest.Pars pars= new ApplyAddRequest.Pars();
            pars.setParCode(parameter.code());
            System.out.println(parameter.code());
            if ( "cmModel".equalsIgnoreCase(parameter.code())) {
                pars.setParValue(item.getModel());
            } else {
                pars.setParValue(parameter.value());
            }

            parsList.add(pars);
        }

        for (ApplyAddRequest.Pars pars : parsList) {
            System.out.println(pars.getParCode() + ": " + pars.getParValue());
        }

        request.setPars(parsList);


        /**
         * packingList	String	N
         * packingListName	String	N	电脑	    装箱清单名单
         * packingListQty	String	N	1	    装箱清单名单数量
         */
        List<ApplyAddRequest.PackingList> packingList = new ArrayList<>();
        ApplyAddRequest.PackingList packingList1 = new ApplyAddRequest.PackingList();
        packingList1.setPackingListName("主产品");
        packingList1.setPackingListQty("1");
        packingList.add(packingList1);
        request.setPackingList(packingList);


        // 商品图片 urlA~urlE
        List<ApplyAddRequest.SupplierImgUrl> supplierImgUrls = new ArrayList<>();
        request.setSupplierImgUrl(supplierImgUrls);
        ApplyAddRequest.SupplierImgUrl supplierImgUrl = new ApplyAddRequest.SupplierImgUrl();
        supplierImgUrls.add(supplierImgUrl);

        String image = "";
        supplierImgUrl.setUrlA(image);
        supplierImgUrl.setUrlB(image);
        supplierImgUrl.setUrlC(image);
        supplierImgUrl.setUrlD(image);
        supplierImgUrl.setUrlE(image);

        List<ParameterLoader.Parameter> commonParameters = parameterLoader.loadCommonParameters(category);
        // 含有通子码 需要添加子型号
        if ( commonParameters!=null && commonParameters.size() > 0 ) {
            List<ApplyAddRequest.ChildItem> childItems = new ArrayList<>();
            request.setChildItem(childItems);

            ApplyAddRequest.ChildItem childItem = new ApplyAddRequest.ChildItem();
            childItems.add(childItem);

            List<ApplyAddRequest.ParsX> parsX = new ArrayList<>();
            childItem.setParsX(parsX);

            for (ParameterLoader.Parameter commonParameter : commonParameters) {
                ApplyAddRequest.ParsX parx = new ApplyAddRequest.ParsX();
                parx.setParCodeX(commonParameter.code());
                parx.setParValueX(commonParameter.value());
                parsX.add(parx);
            }
        }

        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        logger.info("ResParams: " + request.getResParams());

        try {
            ApplyAddResponse response = client.excute(request);
            logger.info("ApplyAddResponse: " + response.getBody());
            SuningResponse.SnError error = response.getSnerror();
            if ( error!=null ) {
                System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                throw new Exception(error.getErrorCode() + " ==> " + error.getErrorMsg());
            } else {
                System.out.println(new Gson().toJson(response.getSnbody().getAddApply()));
            }
        } catch (SuningApiException e) {
           logger.error("[Item: " + item.getItemCode() + "]Exception: " + e.getMessage());
           throw e;
        }
    }

    public static void main(String[] args) throws Exception {

        ShelvesService shelvesService = new SuningShelvesService();

        String code = "10D2148";

        ShelvesItem item = SQLExecutor.executeQueryBean("select * from SHELVESITEM where ITEMCODE = ?", ShelvesItem.class, code);
        shelvesService.shelves(item);

    }

}
