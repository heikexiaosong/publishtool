package com.gavel.shelves.suning;

import com.gavel.config.APPConfig;
import com.gavel.entity.ShelvesItem;
import com.gavel.shelves.CatetoryBrand;
import com.gavel.shelves.ParameterLoader;
import com.gavel.shelves.ShelvesItemParser;
import com.gavel.shelves.ShelvesService;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SuningShelvesService implements ShelvesService {


    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Logger logger = LoggerFactory.getLogger(SuningShelvesService.class);

    private final int moq;

    private final String defaultImage;


    public SuningShelvesService(int moq, String _defaultImage) {
        this.defaultImage = _defaultImage;
        this.moq = moq;
    }

    @Override
    public void shelves(ShelvesItem item) throws Exception {

        if ( item==null ) {
           throw new Exception("Item 不能为空");
        }

        String category = StringUtils.trim(item.getMappingcategorycode());
        String brand = StringUtils.trim(item.getMappingbrandcode());

        if ( StringUtils.isBlank(category)|| StringUtils.isBlank(brand) ) {
            CatetoryBrand catetoryBrand =  new SuningCatetoryBrandSelector().selectCatetoryBrand(item.getCategoryCode(), item.getBrandCode());
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

        // 商品属性设置
        ParameterLoader parameterLoader = new SuningParameterLoader();
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


        System.out.println(".........................");

        List<String> images = ShelvesItemParser.getImages(item.getSkuCode(), defaultImage);
        //supplierImgUrl.setUrlA("http://uimgproxy.suning.cn/uimg1/sop/commodity/hGNS4YLJwso9wdGwpT1JSg.jpg");
//        if ( images==null || images.size() ==0) {
//            throw  new Exception("商品缺少图片");
//        }

        for (String image : images) {
            System.out.println("Image: " + image);
        }

        if ( images.size() >= 1 ) {
            supplierImgUrl.setUrlA(images.get(0));
        }
        if ( images.size() >= 2 ) {
            supplierImgUrl.setUrlB(images.get(1));
        }
        if ( images.size() >= 3 ) {
            supplierImgUrl.setUrlC(images.get(2));
        }

        if ( images.size() >= 4 ) {
            supplierImgUrl.setUrlD(images.get(3));
        }
        if ( images.size() >= 5 ) {
            supplierImgUrl.setUrlE(images.get(4));
        }

        List<ParameterLoader.Parameter> commonParameters = parameterLoader.loadCommonParameters(category);
        // 含有通子码 需要添加子型号
        if ( commonParameters!=null && commonParameters.size() > 0 ) {

            List<ApplyAddRequest.ChildItem> childItems = new ArrayList<>();
            request.setChildItem(childItems);

            ApplyAddRequest.ChildItem childItem = new ApplyAddRequest.ChildItem();
            childItems.add(childItem);


            if ( images==null || images.size() ==0) {
                throw  new Exception("[通子码商品]缺少商品图片");
            }
            //

            if ( images.size() > 0 ) {
                childItem.setSupplierImgAUrl(images.get(0));
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
                introduction = ShelvesItemParser.buildIntroduction(item, moq);
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

                Files.write(request.getResParams().getBytes(), new File(item.getItemCode() + ".request"));

                System.out.println(request.getResParams());
                System.out.println(new Gson().toJson(response));

                Files.write(new Gson().toJson(response).getBytes(), new File(item.getItemCode() + ".response"));

                throw buildException(error.getErrorCode(), error.getErrorMsg());
            } else {
                System.out.println(new Gson().toJson(response.getSnbody().getAddApply()));
            }
        } catch (SuningApiException e) {
           logger.error("[Item: " + item.getItemCode() + "]Exception: " + e.getMessage());
           throw e;
        }
    }

    public static void main(String[] args) throws Exception {

        ShelvesItemParser.getImages("10G1477", null);

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


    public static void main1(String[] args) {


        List<String> err = new ArrayList<>();


     err.add("biz.selfmarket.addapply.length-overlong:*	参数的值超过规定长度	请检查此参数的值,*号表示参数字段名");
     err.add("biz.selfmarket.addapply.missing-parameter:*	参数必填	请检查此参数的值,*号表示参数字段名");
     err.add("isp.sys.service.unavailable.mcmp	服务不可用	请联系苏宁技术人员");
     err.add("biz.selfmarket.addapply.invalid-biz:100	子码条形码重复	子码条形码其他商品重复，请替换");
     err.add("biz.selfmarket.addapply.invalid-biz:101	商品详情错误	校验introduction字段");
     err.add("biz.selfmarket.addapply.invalid-biz:102	条形码重复	条形码和其他商品重复，请替换");
     err.add("biz.selfmarket.addapply.invalid-biz:103	缺少条形码资质图	填写缺少条形码资质图-barpic");
     err.add("biz.selfmarket.addapply.invalid-biz:104	条形码资质图不合规范	条形码资质图不合规范");
     err.add("biz.selfmarket.addapply.invalid-biz:105	条形码资质图不属于商家	条形码资质图不属于商家");
     err.add("biz.selfmarket.addapply.invalid-biz:106	条形码资质图大小过大	条形码资质图大小过大");
     err.add("biz.selfmarket.addapply.invalid-biz:107	条形码资质图长或宽小于800px	条形码资质图长或宽小于800px");
     err.add("biz.selfmarket.addapply.invalid-biz:108	子码条形码资质图不属于商家	子码条形码资质图不属于商家");
     err.add("biz.selfmarket.addapply.invalid-biz:109	子码条形码资质图不属于商家	子码条形码资质图不属于商家");
     err.add("biz.selfmarket.addapply.invalid-biz:110	子码条形码资质图长宽大于800px	子码条形码资质图长宽大于800px");
     err.add("biz.selfmarket.addapply.invalid-biz:111	缺少运营模块	缺少运营模块");
     err.add("biz.selfmarket.addapply.invalid-biz:112	运营模块和自定义模块的名称不能一样	运营模块和自定义模块的名称不能一样");
     err.add("biz.selfmarket.addapply.invalid-biz:113	既不是运营模块，也不是自定义模块	既不是运营模块，也不是自定义模块");
     err.add("biz.selfmarket.addapply.invalid-biz:114	自定义 id必为空，名称和内容必填	自定义 id必为空，名称和内容必填");
     err.add("biz.selfmarket.addapply.invalid-biz:115	自定义模块个数大于10个	自定义模块个数大于10个");
     err.add("biz.selfmarket.addapply.invalid-biz:116	所填运营模块id不在模板当中	所填运营模块id不在模板当中");
     err.add("biz.selfmarket.addapply.invalid-biz:117	运营模板中必填的模块未填写值	运营模板中必填的模块未填写值");
     err.add("biz.selfmarket.addapply.invalid-biz:118	不允许自定义顺序	不允许自定义顺序");
     err.add("biz.selfmarket.addapply.invalid-biz:119	content必须是base64加密	content必须是base64加密");
     err.add("biz.selfmarket.addapply.invalid-biz:120	num重复，或者不为数字	num重复，或者不为数字");
     err.add("biz.selfmarket.addapply.invalid-biz:121	自定义模块名含有敏感词	自定义模块名含有敏感词");
     err.add("biz.selfmarket.addapply.invalid-biz:122	报文中的特性值与参数模板中的不符	报文中的特性值与参数模板中的不符");
     err.add("biz.selfmarket.addapply.invalid-biz:123	产地报错	校验country，region，city是否正确");
     err.add("biz.selfmarket.addapply.invalid-biz:124	基本参数和模板不符合	检查基本参数");
     err.add("biz.selfmarket.addapply.invalid-biz:125	入的报文与所给的类目的通子码属性不符合	校验类目和childItem");
     err.add("biz.selfmarket.addapply.invalid-biz:126	子码条形码必填	子码条形码必填");
     err.add("biz.selfmarket.addapply.invalid-biz:127	子码资质图必填	子码资质图必填");
     err.add("biz.selfmarket.addapply.invalid-biz:128	子商品无特性节点	子商品无特性节点");
     err.add("biz.selfmarket.addapply.invalid-biz:129	特性参数值不符合规范	包含，");
     err.add("biz.selfmarket.addapply.invalid-biz:130	商品主图长或宽小于800px	商品主图长或宽小于800px");
     err.add("biz.selfmarket.addapply.invalid-biz:131	子商品主图长或宽小于800px	子商品主图长或宽小于800px");
     err.add("biz.selfmarket.addapply.invalid-biz:132	报错	看错误描述");
     err.add("biz.selfmarket.addapply.invalid-biz:133	所选充值类型不存在	选择正确的充值类型");
     err.add("biz.selfmarket.addapply.invalid-biz:134	活动链接内容存在时生效时间、失效时间、电脑端链接和移动端链接必填");
     err.add("biz.selfmarket.addapply.invalid-biz:135	活动链接失效时间不得小于生效时间");
     err.add("biz.selfmarket.addapply.invalid-biz:136	输入合法的生效时间和失效时间");
     err.add("biz.selfmarket.addapply.invalid-biz:137	活动链接时间异常");
     err.add("biz.selfmarket.addapply.invalid-biz:138	活动关联文案内容超过25");
     err.add("biz.selfmarket.addapply.invalid-biz:139	商品卖点内容包含手机号");
     err.add("biz.selfmarket.addapply.invalid-biz:140	商品卖点内容超过45");
     err.add("biz.selfmarket.addapply.invalid-biz:141	商品卖点包含敏感词");
     err.add("biz.selfmarket.addapply.invalid-biz:142	活动关联文案内容包含手机号");
     err.add("biz.selfmarket.addapply.invalid-biz:143	活动关联文案内容包含敏感词");
     err.add("biz.selfmarket.addapply.invalid-biz:144	电脑端促销链接不允许出现外部链接");
     err.add("biz.selfmarket.addapply.invalid-biz:145	电脑端促销链接长度不能大于256");
     err.add("biz.selfmarket.addapply.invalid-biz:146	移动端促销链接不允许出现外部链接");
     err.add("biz.selfmarket.addapply.invalid-biz:147	移动端促销链接长度不能大于256");
     err.add("biz.selfmarket.addapply.invalid-biz:148	商品名称最多支持60个字");
     err.add("biz.selfmarket.addapply.invalid-biz:149	电脑端促销链接无效");
     err.add("biz.selfmarket.addapply.invalid-biz:150	移动端促销链接无效");
     err.add("biz.selfmarket.addapply.invalid-biz:151	促销卖点存在时生效时间和失效时间必填");
     err.add("biz.selfmarket.addapply.invalid-biz:152	促销卖点生效时间和失效时间异常");
     err.add("biz.selfmarket.addapply.invalid-biz:153	促销卖点超过30");
     err.add("biz.selfmarket.addapply.invalid-biz:154	促销卖点内容包含手机号");
     err.add("biz.selfmarket.addapply.invalid-biz:155	促销卖点包含敏感词");
     err.add("biz.selfmarket.addapply.invalid-biz:156	亮点词首位不能为空");


        for (String s : err) {
//            System.out.println(s);


            String en = s.split("	")[0];

            String zh = s.split("	")[1].trim().replace("\t", " ");
//
//            System.out.println(en);
//            System.out.println(zh);
//



            StringBuilder builder = new StringBuilder();

            builder.append("if ( errorCode.contains(\"" + en + "\") ) { \n");
            builder.append("\treturn new Exception(\"" + zh + "\");\n");
            builder.append("}");



            System.out.println(builder.toString());
        }




    }



}
