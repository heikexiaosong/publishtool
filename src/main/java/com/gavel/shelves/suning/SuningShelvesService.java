package com.gavel.shelves.suning;

import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.ShelvesItem;
import com.gavel.shelves.*;
import com.gavel.utils.StringUtils;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.suning.api.SuningResponse;
import com.suning.api.entity.selfmarket.ApplyAddRequest;
import com.suning.api.entity.selfmarket.ApplyAddResponse;
import com.suning.api.exception.SuningApiException;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class SuningShelvesService implements ShelvesService {


    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Logger logger = LoggerFactory.getLogger(SuningShelvesService.class);

    private final int moq;

    private final String defaultImage;

    private final String logo;

    private BufferedImage logoImage;

    private CatetoryBrandSelector catetoryBrandSelector = new SuningCatetoryBrandSelector();

    public SuningShelvesService(int moq, String _defaultImage) {
        this(moq, _defaultImage, null);
    }

    public SuningShelvesService(int moq, String _defaultImage, String _logo) {
        this.defaultImage = _defaultImage;
        this.moq = moq;
        this.logo = _logo;

        if ( StringUtils.isNotBlank(logo) ) {
            try {
                logoImage = ImageIO.read(new File(logo));
            } catch (Exception e) {
                System.out.println("logo 图片加载失败: " + e.getMessage());
                logoImage = null;
            }
        }
    }

    @Override
    public void shelves(ShelvesItem item) throws Exception {

        if ( item==null ) {
           throw new Exception("Item 不能为空");
        }

        String category = StringUtils.trim(item.getMappingcategorycode());
        String brand = StringUtils.trim(item.getMappingbrandcode());

        if ( StringUtils.isBlank(category)|| StringUtils.isBlank(brand) ) {
            CatetoryBrand catetoryBrand =  catetoryBrandSelector.selectCatetoryBrand(item.getCategoryCode(), item.getBrandCode());
            if (  StringUtils.isBlank(category) ) {
                item.setMappingcategorycode(catetoryBrand.getCategoryCode());
                item.setMappingcategoryname(catetoryBrand.getCategory());
                category = catetoryBrand.getCategoryCode();
            }

            if ( StringUtils.isBlank(brand) ) {
                item.setMappingbrandcode(catetoryBrand.getBrandCode());
                item.setMappingbrandname(catetoryBrand.getBrandZh());
                brand = catetoryBrand.getBrandCode();
            }
        }

        if ( StringUtils.isBlank(item.getMappingcategorycode()) ) {
            throw new Exception("[Item: " + item.getItemCode() + "]上架类目没有设置");
        }

        if ( StringUtils.isBlank(item.getMappingbrandcode()) ) {
            throw new Exception("[Item: " + item.getItemCode() + "]上架品牌没有设置");
        }

        ApplyAddRequest request = new ApplyAddRequest();

        request.setCategoryCode(category);  // 类目编码
        request.setBrandCode(brand);        // 品牌编码
        request.setItemCode(item.getItemCode()); // 供应商商品编码
        request.setProductName(item.getCmTitle());
        request.setCmTitle(item.getCmTitle());         // 商品标题
        request.setSellingPoints(item.getSellingPoints()); // 商品卖点



        Map<String, String> attrs = ShelvesItemParser.parseAttrs(item.getSkuCode());

        Set<String> keys = new HashSet<>(attrs.keySet());

        Map<String, String> _attrs = new HashMap<String, String>();
        for (String key : keys) {
            String value = attrs.get(key);
            System.out.println(key + ": " + value);
            if ( key.contains("外形尺寸") ) {
                String[] datas = value.split("×");
                if ( datas.length > 2 ) {
                    _attrs.put("长度", datas[0]);
                    _attrs.put("宽度", datas[1]);
                    _attrs.put("高度", datas[2]);
                    _attrs.put("尺寸（长×宽×高）", value);
                }
            }
        }

        /**
         * 长度 => {"categoryCode":"R9008653","supplierCode":"10148425","paraTemplateCode":"basic","paraTemplateDesc":"基本参数模板","parCode":"LAENG","parName":"长度","parType":"3","parUnit":"毫米","isMust":"X","options":"null","parOption":[]}
         * 宽度 => {"categoryCode":"R9008653","supplierCode":"10148425","paraTemplateCode":"basic","paraTemplateDesc":"基本参数模板","parCode":"BREIT","parName":"宽度","parType":"3","parUnit":"毫米","isMust":"X","options":"null","parOption":[]}
         * 高度 => {"categoryCode":"R9008653","supplierCode":"10148425","paraTemplateCode":"basic","paraTemplateDesc":"基本参数模板","parCode":"HOEHE","parName":"高度","parType":"3","parUnit":"毫米","isMust":"X","options":"null","parOption":[]}
         */

        // 商品属性设置
        ParameterLoader parameterLoader = new SuningParameterLoader(_attrs);
        List<ParameterLoader.Parameter> parameters = parameterLoader.loadParameters(category);

        List<ApplyAddRequest.Pars> parsList =new ArrayList<ApplyAddRequest.Pars>();
        for (ParameterLoader.Parameter parameter : parameters) {
            ApplyAddRequest.Pars pars= new ApplyAddRequest.Pars();
            pars.setParCode(parameter.code());
            if ( "cmModel".equalsIgnoreCase(parameter.code()) || "001360".equalsIgnoreCase(parameter.code()) ) {
                pars.setParValue(item.getModel());
            } else {
                pars.setParValue(parameter.value());
            }

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
        request.setSupplierImgUrl(supplierImgUrls);
        ApplyAddRequest.SupplierImgUrl supplierImgUrl = new ApplyAddRequest.SupplierImgUrl();
        supplierImgUrls.add(supplierImgUrl);

        List<ShelvesItemParser.Pic> images = ShelvesItemParser.getImages(item.getSkuCode(), defaultImage, logoImage);
        for (ShelvesItemParser.Pic image : images) {
            System.out.println("Image: " + image.getUrl());
        }

        if ( images.size() >= 1 ) {
            supplierImgUrl.setUrlA(images.get(0).getUrl());
        }
        if ( images.size() >= 2 ) {
            supplierImgUrl.setUrlB(images.get(1).getUrl());
        }
        if ( images.size() >= 3 ) {
            supplierImgUrl.setUrlC(images.get(2).getUrl());
        }

        if ( images.size() >= 4 ) {
            supplierImgUrl.setUrlD(images.get(3).getUrl());
        }
        if ( images.size() >= 5 ) {
            supplierImgUrl.setUrlE(images.get(4).getUrl());
        }

        List<ParameterLoader.Parameter> commonParameters = parameterLoader.loadCommonParameters(category);
        // 含有通子码 需要添加子型号
        if ( commonParameters!=null && commonParameters.size() > 0 ) {

            List<ApplyAddRequest.ChildItem> childItems = new ArrayList<>();
            request.setChildItem(childItems);

            ApplyAddRequest.ChildItem childItem = new ApplyAddRequest.ChildItem();
            childItems.add(childItem);
            childItem.setBarcode("0000000000000");

            if ( images==null || images.size() ==0) {
                throw  new Exception("[通子码商品]缺少商品图片");
            }
            //

            if ( images.size() > 0 ) {
                childItem.setSupplierImgAUrl(images.get(0).getUrl());
            }

            List<ApplyAddRequest.ParsX> parsX = new ArrayList<>();
            childItem.setParsX(parsX);

            for (ParameterLoader.Parameter commonParameter : commonParameters) {
                ApplyAddRequest.ParsX parx = new ApplyAddRequest.ParsX();
                parx.setParCodeX(commonParameter.code());
                parx.setParValueX(commonParameter.value());
                parsX.add(parx);
            }
        }


        /**
         * 商家商品介绍，UTF-8格式。将html内容的txt文本文件读取为字节数组,然后base64加密，去除空格回车后作为字段，传输时所涉及的图片不得使用外部url。允许写入CSS（禁止引用外部CSS）不支持JS。
         */

        String introduction = item.getIntroduction();
        try {
            if ( StringUtils.isBlank(introduction)) {
                introduction = ShelvesItemParser.buildIntroduction(item, moq, images);
                item.setIntroduction(introduction);
            }
        } catch (Exception e) {
            System.out.println("[" + item.getItemCode() + "]生成商品详情异常: " + e.getMessage());
        }
        request.setIntroduction(introduction); // 商品介绍

        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        try {
            ApplyAddResponse response = APPConfig.getInstance().client().excute(request);
            logger.info("ApplyAddResponse: " + response.getBody());
            SuningResponse.SnError error = response.getSnerror();
            if ( error!=null ) {
                StringBuilder content =  new StringBuilder("请求报文: \n");
                content.append(request.getResParams()).append("\n\n");
                content.append("响应报文:\n");
                content.append(new Gson().toJson(response)).append("\n");
                Files.write(content.toString().getBytes(), new File("report" + File.separator + item.getItemCode() + ".err"));
                throw buildException(error.getErrorCode(), error.getErrorMsg());
            } else {
                System.out.println(new Gson().toJson(response.getSnbody().getAddApply()));
            }
        } catch (SuningApiException e) {
           logger.error("[Item: " + item.getItemCode() + "]Exception: " + e.getMessage());
           throw e;
        }
    }

    private static Exception buildException(String errorCode, String errorMsg){


        if ( errorCode.contains("biz.custom.item.invalid-biz:supplierImgUrlRepeate") ) {
            return new Exception("供应商商品图片重复");
        }

        if ( errorCode.contains("biz.selfmarket.addapply.length-overlong:") ) {
            String text = errorCode.split("	")[0];
            String name = text.split(":")[1];
            return new Exception("参数[" + name + "]的值超过规定长度");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.missing-parameter:") ) {
            String text = errorCode.split("	")[0];
            String name = text.split(":")[1];
            return new Exception("参数[" + name + "]必填");
        }
        if ( errorCode.contains("isp.sys.service.unavailable.mcmp") ) {
            return new Exception("服务不可用");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:100") ) {
            return new Exception("子码条形码重复");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:101") ) {
            return new Exception("商品详情错误");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:102") ) {
            return new Exception("条形码重复");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:103") ) {
            return new Exception("缺少条形码资质图");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:104") ) {
            return new Exception("条形码资质图不合规范");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:105") ) {
            return new Exception("条形码资质图不属于商家");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:106") ) {
            return new Exception("条形码资质图大小过大");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:107") ) {
            return new Exception("条形码资质图长或宽小于800px");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:108") ) {
            return new Exception("子码条形码资质图不属于商家");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:109") ) {
            return new Exception("子码条形码资质图不属于商家");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:110") ) {
            return new Exception("子码条形码资质图长宽大于800px");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:111") ) {
            return new Exception("缺少运营模块");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:112") ) {
            return new Exception("运营模块和自定义模块的名称不能一样");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:113") ) {
            return new Exception("既不是运营模块，也不是自定义模块");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:114") ) {
            return new Exception("自定义 id必为空，名称和内容必填");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:115") ) {
            return new Exception("自定义模块个数大于10个");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:116") ) {
            return new Exception("所填运营模块id不在模板当中");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:117") ) {
            return new Exception("运营模板中必填的模块未填写值");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:118") ) {
            return new Exception("不允许自定义顺序");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:119") ) {
            return new Exception("content必须是base64加密");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:120") ) {
            return new Exception("num重复，或者不为数字");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:121") ) {
            return new Exception("自定义模块名含有敏感词");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:122") ) {
            return new Exception("报文中的特性值与参数模板中的不符");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:123") ) {
            return new Exception("产地报错");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:124") ) {
            String name = errorCode.replace("biz.selfmarket.addapply.invalid-biz:124:", "");
            return new Exception("基本参数[" + name +"]和模板不符合");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:125") ) {
            return new Exception("入的报文与所给的类目的通子码属性不符合");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:126") ) {
            return new Exception("子码条形码必填");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:127") ) {
            return new Exception("子码资质图必填");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:128") ) {
            return new Exception("子商品无特性节点");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:129") ) {
            return new Exception("特性参数值不符合规范");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:130") ) {
            return new Exception("商品主图长或宽小于800px");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:131") ) {
            return new Exception("子商品主图长或宽小于800px");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:132") ) {
            String msg = errorCode.replace("biz.selfmarket.addapply.invalid-biz:132", "");
            return new Exception(msg);
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:133") ) {
            return new Exception("所选充值类型不存在");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:134") ) {
            return new Exception("活动链接内容存在时生效时间、失效时间、电脑端链接和移动端链接必填");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:135") ) {
            return new Exception("活动链接失效时间不得小于生效时间");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:136") ) {
            return new Exception("输入合法的生效时间和失效时间");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:137") ) {
            return new Exception("活动链接时间异常");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:138") ) {
            return new Exception("活动关联文案内容超过25");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:139") ) {
            return new Exception("商品卖点内容包含手机号");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:140") ) {
            return new Exception("商品卖点内容超过45");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:141") ) {
            return new Exception("商品卖点包含敏感词");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:142") ) {
            return new Exception("活动关联文案内容包含手机号");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:143") ) {
            return new Exception("活动关联文案内容包含敏感词");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:144") ) {
            return new Exception("电脑端促销链接不允许出现外部链接");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:145") ) {
            return new Exception("电脑端促销链接长度不能大于256");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:146") ) {
            return new Exception("移动端促销链接不允许出现外部链接");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:147") ) {
            return new Exception("移动端促销链接长度不能大于256");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:148") ) {
            return new Exception("商品名称最多支持60个字");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:149") ) {
            return new Exception("电脑端促销链接无效");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:150") ) {
            return new Exception("移动端促销链接无效");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:151") ) {
            return new Exception("促销卖点存在时生效时间和失效时间必填");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:152") ) {
            return new Exception("促销卖点生效时间和失效时间异常");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:153") ) {
            return new Exception("促销卖点超过30");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:154") ) {
            return new Exception("促销卖点内容包含手机号");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:155") ) {
            return new Exception("促销卖点包含敏感词");
        }
        if ( errorCode.contains("biz.selfmarket.addapply.invalid-biz:156") ) {
            return new Exception("亮点词首位不能为空");
        }
        return new Exception(errorCode);
    }



    public static void main(String[] args) throws Exception {

        APPConfig.getInstance().getShopinfo();

        SuningShelvesService suningShelvesService = new SuningShelvesService(100, null);


        ShelvesItem shelvesItem = null;

        try {
            shelvesItem = SQLExecutor.executeQueryBean("select * from SHELVESITEM where TASKID =? and SKUCODE = ? ", ShelvesItem.class, "1590027322458", "5G8354");
            suningShelvesService.shelves(shelvesItem);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
