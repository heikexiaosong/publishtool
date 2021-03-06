package com.gavel.shelves.suning;

import com.gavel.HttpUtils;
import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.ImageInfo;
import com.gavel.entity.Item;
import com.gavel.entity.ShelvesItem;
import com.gavel.jd.JDSkuParser;
import com.gavel.jd.SkuPageLoader;
import com.gavel.shelves.*;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.NPicAddRequest;
import com.suning.api.entity.item.NPicAddResponse;
import com.suning.api.entity.selfmarket.ApplyAddRequest;
import com.suning.api.entity.selfmarket.ApplyAddResponse;
import com.suning.api.exception.SuningApiException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SuningShelvesService implements ShelvesService {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Logger logger = LoggerFactory.getLogger(SuningShelvesService.class);

    private final int moq;

    private final String defaultImage;

    private final String logo;

    private BufferedImage logoImage;

    private final String picdir;

    private final String brandCn;

    private final String brandEn;

    private CatetoryBrandSelector catetoryBrandSelector = new SuningCatetoryBrandSelector();

    public SuningShelvesService(int moq, String _defaultImage) {
        this(moq, _defaultImage, null);
    }

    public SuningShelvesService(int moq, String _defaultImage, String _logo) {
       this(moq, _defaultImage, _logo, null, null, null);
    }

    public SuningShelvesService(int moq, String _defaultImage, String _logo, String picdir, String brandCn, String brandEn) {
        this.defaultImage = _defaultImage;
        this.moq = moq;
        this.logo = _logo;
        this.picdir = picdir;
        this.brandCn = brandCn;
        this.brandEn = brandEn;

        if ( StringUtils.isNotBlank(logo) ) {
            try {
                logoImage = ImageIO.read(new File(logo));
            } catch (Exception e) {
                System.out.println("logo 图片加载失败: " + e.getMessage());
                logoImage = null;
            }
        }

        if ( picdir!=null && picdir.trim().length() > 0 ) {
            File dir = new File(picdir);
            if ( !dir.exists() ) {
                dir.mkdirs();
            }
        }
    }


    /**
     * 固安捷商品上架
     * @param item
     * @throws Exception
     */
    @Override
    public void shelves(ShelvesItem item) throws Exception {

        if ( item==null ) {
           throw new Exception("Item 不能为空");
        }

        if ( "JD".equalsIgnoreCase(item.getType()) ) {
            shelvesJDSku(item);
            return;
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

        if ( StringUtils.isNotBlank(item.getSrc()) ) {
            byte[] datas = Base64.decodeBase64(introduction.getBytes("UTF8"));
            String _introduction = new String(datas, "UTF8");
            _introduction = _introduction.replace(item.getSrc(), item.getDest());
            request.setIntroduction(Base64.encodeBase64String(_introduction.getBytes("UTF8"))); // 商品介绍
        }


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

    /**
     * JD 商品上架
     * @param item
     * @throws Exception
     */
    public void shelvesJDSku(ShelvesItem item) throws Exception {

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

        if ( item.getCmTitle()!=null && item.getCmTitle().length() > 60  ) {
            throw  new RuntimeException("商品标题长度超过60");
        }


        Map<String, String> _attrs = new HashMap<String, String>();
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



        Item _item = null;
        String html = null;
        try {
            _item = SQLExecutor.executeQueryBean("select * from ITEM where CODE = ? ", Item.class, item.getSkuCode());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if ( _item!=null && _item.getUrl()!=null ) {
            html =  SkuPageLoader.getInstance().loadPage(item.getSkuCode(), _item.getUrl());
        } else {
            html =  SkuPageLoader.getInstance().loadPage(item.getSkuCode());
        }

        if ( html==null || html.trim().length()==0 ) {
            throw new Exception("[Item: " + item.getItemCode() + "]获取sku页面信息失败");
        }

        List<String> columnValues = new ArrayList<>();
        List<String> detailUrls = new ArrayList<>();
        /// ================

        Document doc = Jsoup.parse(html);
        Element crumb = doc.selectFirst("div#crumb-wrap .crumb");
        if ( crumb==null ) {
            throw new Exception("Html内容有异常");
        }


        boolean coup = false;
        if ( _item!= null ) {
            float _price = _item.getPrice();
            Element price = doc.selectFirst("span.p-price .price");
            if ( price==null ) {
               try {
                   _price = Float.parseFloat(price.text());
                   System.out.println(_price);
               } catch (Exception e) {

               }
            }

            Element page_hx_price = doc.selectFirst("del#page_hx_price");
            if ( page_hx_price!=null ) {
                coup = true;
                try {
                    _price = Float.parseFloat(page_hx_price.text().replace(",", "").replace("￥", ""));
                    System.out.println(_price);
                } catch (Exception e) {

                }
            }

            if ( _price != _item.getPrice() ) {
                SQLExecutor.update(_item);
            }
        }

        Element ellipsis = crumb.selectFirst("div.ellipsis");

        crumb.select("div.sep").remove();

        if ( item.getSellingPoints()==null || item.getSellingPoints().trim().length()<=0 ) {
            String sellPoint = (ellipsis==null ?  item.getCmTitle() : ellipsis.text());
            if ( sellPoint!=null && sellPoint.length() > 45 ) {
                sellPoint = sellPoint.substring(0, 45);
            }
            item.setSellingPoints(sellPoint);

            try {
                SQLExecutor.update(item);
            } catch (Exception e) {

            }

            System.out.println("商品卖点: " + request.getSellingPoints());
        }

        request.setSellingPoints(item.getSellingPoints()); // 商品卖点
        if ( item.getSellingPoints()!=null && item.getSellingPoints().length() > 45  ) {
            throw  new RuntimeException("商品卖点长度超过45");
        }


        Element detail = doc.selectFirst("div#detail");
        if ( detail!=null ) {
            Element parameter = detail.selectFirst("div.p-parameter");

            Elements parameter2 = parameter.select("ul.parameter2  li");
            for (Element element : parameter2) {
                if ( element.text().startsWith("商品编号")  || element.text().startsWith("店铺") ) {
                    System.out.println(element.text() + " ... X");
                } else {
                    System.out.println(element.text());
                    columnValues.add(element.text());
                }
            }


            Element itemdetail = detail.selectFirst("div.item-detail");
            if ( itemdetail!=null ) {
                System.out.println(itemdetail.text());

                columnValues.add(itemdetail.text());
            }


            // 规格与包装
            Elements ptableItems = detail.select("div.Ptable-item dl");
            if ( ptableItems!=null && ptableItems.size() > 0 ) {

                for (Element ptableItem : ptableItems) {

                    Element tips = ptableItem.selectFirst("dd.Ptable-tips");
                    if ( tips!=null ) {
                        tips.remove();
                    }

                    String line = "";
                    for (Element element : ptableItem.children()) {
                        if ( element.is("dd") ) {
                            System.out.println(line);

                            line = line + element.text();
                            columnValues.add(line);
                            line = null;
                        } else {
                            line = element.text() + ": ";
                        }
                    }

                    if ( line!=null && line.trim().length() > 0 ) {
                        columnValues.add(line);
                    }
                }
            }

            // package-list
            Element packagelist = detail.selectFirst("div.package-list");
            if ( packagelist!=null ) {
                if ( packagelist.text().contains("暂无") ) {
                    System.out.println(packagelist.text() + ".....X");
                } else {
                    System.out.println(packagelist.text());
                    columnValues.add(packagelist.text());
                }
            }

        }




        Element ssd = detail.selectFirst("div#ssd-vc-goods");
        if ( ssd!=null ) {
            ssd.remove();
        }

        Elements detailTexts = detail.select("div#J-detail-content div p");
        if ( detailTexts!=null ) {
            for (Element detailText : detailTexts) {
                columnValues.add(detailText.text());
            }
        }


        // 商品图片 urlA~urlE
        List<ApplyAddRequest.SupplierImgUrl> supplierImgUrls = new ArrayList<>();
        request.setSupplierImgUrl(supplierImgUrls);
        ApplyAddRequest.SupplierImgUrl supplierImgUrl = new ApplyAddRequest.SupplierImgUrl();
        supplierImgUrls.add(supplierImgUrl);


        JDSkuParser jdSkuParser = new JDSkuParser(item, html);

        // 商品主图片
        List<ImageInfo> imageInfos = jdSkuParser.loadImages(picdir);

        List<String> images = preHandleImage(imageInfos, logoImage, new File(picdir), 5);

        if ( images==null || images.size() <= 0 ) {
            throw  new RuntimeException("没有找到商品主图");
        }

        //List<ShelvesItemParser.Pic> images = ShelvesItemParser.getImages(item.getSkuCode(), picUrls, defaultImage, logoImage);

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
            childItem.setBarcode("0000000000000");

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

        String orgBandZn = "";
        String orgBandEn = "";
        Element brandEle = doc.selectFirst("ul#parameter-brand a");
        if ( brandEle!=null ) {
           String[] brands = brandEle.text().split("[（）]");
           if ( brands!=null ) {
               if ( brands.length > 0 ) {
                   orgBandZn = brands[0];
               }
               if ( brands.length > 1 ) {
                   orgBandEn = brands[1];
               }
           }
        }


        String introduction = item.getIntroduction();
        try {

            List<ImageInfo> _imageInfos = jdSkuParser.loadDetailImages(picdir);
            for (ImageInfo imageInfo : _imageInfos) {
                detailUrls.add(imageInfo.getPicurl());
            }

            List<String> _images = preHandleImage(_imageInfos, null, new File(picdir), -1);

            System.out.println("商品详情图: " + detailUrls.size());

            introduction = buildIntroduction(moq, item.getPrice(), _images, columnValues);
        } catch (Exception e) {
            System.out.println("[" + item.getItemCode() + "]生成商品详情异常: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }


        if ( StringUtils.isNotBlank(item.getSrc()) ) {
            introduction = introduction.replace(item.getSrc(), item.getDest());
        }

        if ( (StringUtils.isNotBlank(orgBandZn) || StringUtils.isNotBlank(orgBandEn))
              && (StringUtils.isNotBlank(brandCn) || StringUtils.isNotBlank(brandEn))   ) {


            String fullBrand = brandCn + "（" + brandEn + "）";
            if ( StringUtils.isBlank(brandCn) ) {
                fullBrand = brandEn;
            }

            if ( StringUtils.isBlank(brandEn) ) {
                fullBrand = brandCn;
            }

            if ( StringUtils.isBlank(orgBandEn) ) {
                introduction = introduction.replace(orgBandZn, fullBrand);
            } else {
                introduction = introduction.replace(orgBandZn, brandCn);
                introduction = introduction.replace(orgBandEn, brandEn);
            }

            // 品牌1 b1 -> 京苏 jinsu
            // 品牌1 b1 -> 京苏 jinsu
        }

        request.setIntroduction(Base64.encodeBase64String(introduction.getBytes("UTF8"))); // 商品介绍


        System.out.println(request.getResParams());
        try {
            item.setZt(1);
            SQLExecutor.update(item);
        } catch (Exception e) {

        }

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
                System.out.println(request.getResParams());
                System.out.println(new Gson().toJson(response.getSnbody().getAddApply()));
            }
        } catch (SuningApiException e) {
            logger.error("[Item: " + item.getItemCode() + "]Exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 图片预处理 图片上传到苏宁图片空间
     *
     * @param imageInfos
     * @param logoImage
     * @return
     */
    private List<String> preHandleImage(List<ImageInfo> imageInfos, BufferedImage logoImage, File dir, int total) {
        List<ImageInfo> usable = new ArrayList<>();
        try {
            System.out.println("待处理图片: " + (imageInfos == null ? 0 : imageInfos.size()));
            if (imageInfos == null || imageInfos.size() == 0) {
                return Collections.EMPTY_LIST;
            }

            boolean preHandl = true;
            try {
                ShelvesItem shelvesItem = SQLExecutor.executeQueryBean("select * from SHELVESITEM where ID = ? ", ShelvesItem.class, imageInfos.get(0).getRefid());
                preHandl = (shelvesItem.getZt()==1);
            } catch (Exception e) {

            }

            Collections.sort(imageInfos, new Comparator<ImageInfo>() {
                @Override
                public int compare(ImageInfo o1, ImageInfo o2) {
                    return o1.getXh() - o2.getXh();
                }
            });

            for (ImageInfo imageInfo : imageInfos) {
                try {

                    if (  preHandl && StringUtils.isBlank(imageInfo.getFilepath())) {
                        System.out.println("[Image][" + imageInfo.getCode() + "][" + imageInfo.getXh() + "]Filepath is blank.");
                        continue;
                    }

                    File imageFile = new File(imageInfo.getFilepath());
                    if ( preHandl && !imageFile.exists() ) {
                        System.out.println("[Image][" + imageInfo.getCode() + "][" + imageInfo.getXh() + "][File: " + imageInfo.getFilepath() + "] not exist.");
                        continue;
                    }

                    if (StringUtils.isNotBlank(imageInfo.getSuningurl()) && imageInfo.getStatus() < 999 ) {
                        continue;
                    }

                    if (StringUtils.isNotBlank(imageInfo.getSuningurl())) {
                        try {
                            long len = HttpUtils.imageLength(imageInfo.getSuningurl() + "?version=1");
                            System.out.println("[" + imageInfo.getSuningurl() + "]Len: " + len);
                            imageInfo.setStatus((int)len);
                            if (len > 999) {
                                usable.add(imageInfo);
                            }
                            continue;
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }

                    if (StringUtils.isNotBlank(imageInfo.getFilepath())) {
                        if ( logoImage!=null && total > 0 ) {
                            try {
                                // ImageIO读取图片
                                BufferedImage pic = ImageIO.read( new File(imageInfo.getFilepath()));
                                Thumbnails.of(pic)
                                        // 设置图片大小
                                        .size(pic.getWidth(), pic.getHeight())
                                        // 加水印 参数：1.水印位置 2.水印图片 3.不透明度0.0-1.0
                                        .watermark(Positions.TOP_LEFT, logoImage, 0.9f)
                                        // 输出到文件
                                        .toFile(imageInfo.getFilepath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                        NPicAddRequest request = new NPicAddRequest();
                        request.setPicFileData(imageInfo.getFilepath());
                        NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                        SuningResponse.SnError error = response.getSnerror();
                        if (error != null) {
                            System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                            System.out.println(new Gson().toJson(response));
                        } else {
                            System.out.println(new Gson().toJson(response.getSnbody().getAddNPic()));
                            imageInfo.setSuningurl(response.getSnbody().getAddNPic().getPicUrl());
                            usable.add(imageInfo);
                        }
                    }

                } finally {
                    try {
                        SQLExecutor.update(imageInfo);
                    } catch (Exception e) {

                    }
                }
            }
        }catch (Exception e) {

        }

        if ( total > 0 && usable!=null && usable.size() > 0 ) {
            int start = imageInfos.size() + 1;
            while (  usable.size() < total ) {  // 图片不全 补主图
                int offset = start -  imageInfos.size() - 1;

                ImageInfo imageInfo = usable.get(offset);
                File org_image = new File(imageInfo.getFilepath());

                ImageInfo supplement = new ImageInfo();
                String id = MD5Utils.md5Hex(supplement.getCode() + "_" + imageInfo.getType() + "_" + start + "_" + System.currentTimeMillis());
                supplement.setId(id);
                supplement.setRefid(imageInfo.getRefid());
                supplement.setCode(imageInfo.getCode());
                supplement.setType(imageInfo.getType());
                supplement.setPicurl(imageInfo.getPicurl());
                supplement.setXh(start);
                String imageFileName =  supplement.getCode() + "_" + start + ".jpg";
                File targetImage = new File(dir, imageFileName);
                try {

                    Mat src = Imgcodecs.imread(org_image.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
                    if ( src.width()!=800 || src.height()!=800 ) {
                        Imgproc.resize(src, src, new Size(800, 800));
                    }
                    double[] pixes = src.get(src.width()-30, src.height()-10);
                    Scalar scalar =  new Scalar(238,  238,  238);
                    if ( pixes!=null && pixes.length==3 ) {
                        pixes[0] = pixes[0]-15;
                        pixes[1] = pixes[1]-15;
                        pixes[2] = pixes[2]-15;
                        scalar = new Scalar(pixes);
                    }
                    Imgproc.putText(src, Integer.toString(start), new Point(src.width()-30, src.height()-10), 0, 1.0, scalar);
                    Imgcodecs.imwrite(targetImage.getAbsolutePath(), src);
                    supplement.setFilepath(targetImage.getAbsolutePath());


                    NPicAddRequest request = new NPicAddRequest();
                    request.setPicFileData(targetImage.getAbsolutePath());
                    NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                    SuningResponse.SnError error = response.getSnerror();
                    if (error != null) {
                        System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                        System.out.println(new Gson().toJson(response));
                    } else {
                        System.out.println(new Gson().toJson(response.getSnbody().getAddNPic()));
                        supplement.setSuningurl(response.getSnbody().getAddNPic().getPicUrl());
                    }
                    usable.add(supplement);
                    SQLExecutor.insert(supplement);
                    start++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        List<String> images = usable.stream().map( ImageInfo::getSuningurl).collect(Collectors.toList());


        return images;
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


    public static String buildIntroduction(int moq, float _price, List<String> detailUrls, List<String> columnValues) throws Exception {

//        Map<String, String> detailImageMap = new HashMap<>();
//        for (String picUrl : detailUrls) {
//            if ( picUrl.contains("566010f4N01f5d17a.png") ) {
//                continue;
//            }
//            String picSuningUrl = ShelvesItemParser.uploadDetailImage(picUrl);
//            if (com.gavel.utils.StringUtils.isNotBlank(picSuningUrl)) {
//                detailImageMap.put(picUrl, picSuningUrl);
//            }
//        }


        StringBuilder detail = new StringBuilder();
        if ( _price > 0 && _price < moq ) {
            detail.append("<div class=\"box\">");
            detail.append("<div style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\">" +
                    "<span>商品起定量</span><span style=\"color:red;\">(请按起订量拍，否则无法发货)</span></div>");

            detail.append(" <span style=\"color:red;\">起订量： ").append( (int)Math.ceil(100/_price)).append("</span><br>");
//            if ( number.contains(unit) ) {
//                detail.append(" 包装数量： ").append(number).append("<br>");
//            } else {
//                detail.append(" 包装数量： ").append(number).append("/").append(unit).append("<br>");
//            }

            detail.append("</div>");
        }

        detail.append("<div class=\"box\">");


        detail.append("<div style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品规格</div>");


        //detail.append("•").append("制造商型号： ").append(model).append("<br>");
         //= new HashMap<>();

        for (String columnValue : columnValues) {
            detail.append("•").append(columnValue);
            detail.append("<br>");
        }
//        for (String key : columnValues.keySet()) {
//            detail.append("•").append(key).append(": ").append(columnValues.get(key));
//            detail.append("<br>");
//        }

        detail.append("<div  style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品描述</div>");
        //detail.append(proDetailTit.html());

        System.out.println("详情图片: " + detailUrls.size());
        if ( detailUrls.size() > 0 ) {
            detail.append("<br>");
            for (String picUrl : detailUrls) {
                detail.append("<img alt=\"\" src=\"" + picUrl + "\">");
                detail.append("<br>");
            }
        }
        detail.append("</div>");



//        List<String> picUrls = new ArrayList<>();
//        if ( images!=null && images.size() > 0 ) {
//            for (ShelvesItemParser.Pic image : cc) {
//                if ( !image.isIscreate() &&  com.gavel.utils.StringUtils.isNotBlank(image.getUrl()) ){
//                    picUrls.add(image.getUrl());
//                }
//            }
//
//        }


//        detail.append("<div  style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品图片</div>");
//        if ( picUrls.size() > 0 ) {
//            for (String picUrl : picUrls) {
//                detail.append("<p><img alt=\"\" src=\"" + picUrl + "\" class=\"product\"></p>");
//            }
//        }
        detail.append("<p><img alt=\"\" src=\"https://uimgproxy.suning.cn/uimg1/sop/commodity/MhdqxYCAnkWz57dhaZS4PQ.jpg\" class=\"product\"></p>");
        detail.append("</div>");



        System.out.println("Detail: " + detail.toString());

        return detail.toString();
    }



    public static void main(String[] args) throws Exception {

        APPConfig.getInstance().getShopinfo();

        SuningShelvesService suningShelvesService = new SuningShelvesService(100, null);


        ShelvesItem shelvesItem = null;

        try {
            shelvesItem = SQLExecutor.executeQueryBean("select * from SHELVESITEM where TASKID =? and SKUCODE = ? ", ShelvesItem.class, "1593958053079", "15265522094");
            suningShelvesService.shelvesJDSku(shelvesItem);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
