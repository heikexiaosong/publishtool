package com.gavel.shelves.suning;

import com.gavel.entity.Item;
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
    public void shelves(Item item) throws Exception {

        if ( item==null ) {
           throw new Exception("Item 不能为空");
        }

        /**
         * 商家商品介绍，UTF-8格式。将html内容的txt文本文件读取为字节数组,然后base64加密，去除空格回车后作为字段，传输时所涉及的图片不得使用外部url。允许写入CSS（禁止引用外部CSS）不支持JS。
         */
        String introduction = "";


        ApplyAddRequest request = new ApplyAddRequest();

        request.setCategoryCode(item.getCategory());  // 类目编码
        request.setBrandCode(item.getBrand());        // 品牌编码
        request.setItemCode(item.getCode()); // 供应商商品编码
        request.setProductName(item.getProductname());
        request.setCmTitle(item.getName());         // 商品标题
        request.setSellingPoints(item.getSubname()); // 商品卖点

        request.setIntroduction(introduction); // 商品介绍

        // 商品属性设置
        ParameterLoader parameterLoader = new SuningParameterLoader();
        List<ParameterLoader.Parameter> parameters = parameterLoader.loadParameters(item.getCategory());

        List<ApplyAddRequest.Pars> parsList =new ArrayList<ApplyAddRequest.Pars>();
        for (ParameterLoader.Parameter parameter : parameters) {
            ApplyAddRequest.Pars pars= new ApplyAddRequest.Pars();
            pars.setParCode(parameter.code());
            pars.setParValue(parameter.value());
            parsList.add(pars);
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
        ApplyAddRequest.SupplierImgUrl supplierImgUrl = new ApplyAddRequest.SupplierImgUrl();

        String image = "";
        supplierImgUrl.setUrlA(image);
        supplierImgUrl.setUrlB(image);
        supplierImgUrl.setUrlC(image);
        supplierImgUrl.setUrlD(image);
        supplierImgUrl.setUrlE(image);
        supplierImgUrls.add(supplierImgUrl);
        request.setSupplierImgUrl(supplierImgUrls);

        List<ParameterLoader.Parameter> commonParameters = parameterLoader.loadCommonParameters(item.getCategory());
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
           logger.error("[Item: " + item.getCode() + "]Exception: " + e.getMessage());
           throw e;
        }
    }

}
