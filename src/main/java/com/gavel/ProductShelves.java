package com.gavel;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.ImageCache;
import com.gavel.entity.Item;
import com.gavel.entity.Itemparameter;
import com.gavel.grainger.StringUtils;
import com.gavel.shelves.CatetoryBrand;
import com.gavel.shelves.CatetoryBrandSelector;
import com.gavel.shelves.suning.SuningCatetoryBrandSelector;
import com.gavel.suning.SuningClient;
import com.gavel.utils.ImageLoader;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.NPicAddRequest;
import com.suning.api.entity.item.NPicAddResponse;
import com.suning.api.entity.selfmarket.ApplyAddRequest;
import com.suning.api.entity.selfmarket.ApplyAddResponse;
import com.suning.api.exception.SuningApiException;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class ProductShelves {


    private  static  final DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");

    /**
     *
     *  合成新标题
     *  *
     * @param title
     * @param branda
     * @param brandb
     * @param model
     * @param number
     * @return
     */
    public static String title(String title, String branda, String brandb, String model, String number){


        String brandc = brandb;
        if ( brandb!=null ){
            brandc = brandb.replace(branda, "").trim();
        }

         title = title.trim();
         if ( title.startsWith(brandb.trim()) ) {
             title = title.replace(brandb.trim(), "").trim();
         }
        if ( title.startsWith(brandc.trim()) ) {
            title = title.replace(brandc.trim(), "").trim();
        }
         if ( title.startsWith(branda.trim()) ) {
            title = title.replace(branda.trim(), "").trim();
         }

        if ( title.startsWith(model.trim()) ) {
            title = title.replace(model.trim(), "").trim();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(brandb);

        if ( builder.length() + model.length() + title.length() < 60 ) {
            builder.append(" ").append(model);
        }
        builder.append(" ").append(title);

        boolean preBlank = true;
        for (int i = 0; i < builder.length(); i++) {
            char c = builder.charAt(i);
            if ( c!=' '){
                preBlank=false;
                continue;
            }
            if ( preBlank ){
                builder.deleteCharAt(i);
                i--;
            }
            preBlank = true;
        }


        if ( builder.length() < 50 && number!=null && number.trim().length()>0 ) {
            builder.append("(包装数量 ").append(number).append(")");
        }

        while ( builder.length() > 60 ) {
            for (int i = 0; i < builder.length(); i++) {
                char c = builder.charAt(i);
                if ( c==' ' && i > 0){
                    builder = builder.delete(0, i);
                    break;
                }
            }
        }

        return builder.toString();
    }

    public static void main(String[] args) throws Exception {


        List<Item> items = SQLExecutor.executeQueryBeanList("select * from ITEM where PRODUCTCODE = '291735'", Item.class);
        String suningBrand = "285R";
        String suningCate = "R9002847";
        for (Item item : items) {
            String code = "5W4470";
            HtmlCache htmlCache = loadHtmlPage("https://www.grainger.cn/u-" + code + ".html", null);
            if ( htmlCache==null || htmlCache.getHtml().trim().length() <=0 ) {
                System.out.println("Html 获取失败。。");
                return;
            }

            run(htmlCache, suningCate, suningBrand);

            break;
        }


    }


    public static HtmlCache loadHtmlPage(String url, String params) throws Exception {

        HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, url);
        if ( cache == null ) {
            StringBuilder urlBuilder = new StringBuilder(url.trim());
            if ( params!=null && params.trim().length() > 0 ){
                urlBuilder.append(params.trim());
            }

            String content = HttpUtils.get(urlBuilder.toString().trim(), "https://www.grainger.cn");
            if ( content==null || content.trim().length()==0 ) {
                return null;
            }

            cache = new HtmlCache();
            cache.setUrl(url.trim());
            cache.setHtml(content);
            cache.setContentlen(content.length());
            cache.setUpdatetime(Calendar.getInstance().getTime());
            SQLExecutor.insert(cache);
        }
        return cache;
    }

    /**
     * 图片上传
     *
     * @throws Exception
     */
    public static String uploadImage(String url) throws Exception {


        ImageCache image = ImageLoader.loadIamge(url);
        if ( image==null || image.getFilepath()==null || image.getFilepath().trim().length()==0 ) {
            return null;
        }

        if ( image.getPicurl()!=null && image.getPicurl().trim().length() > 0 ) {
            return image.getPicurl();
        }

        String picUrl = null;
        {
            String localFilePath = ImageLoader.PICS_DIR + File.separator + image.getFilepath();
            NPicAddRequest request = new NPicAddRequest();
            request.setPicFileData(localFilePath);
            try {
                NPicAddResponse response = client.excuteMultiPart(request);
                System.out.println("ApplyAddRequest :" + response.getBody());
                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    picUrl = response.getSnbody().getAddNPic().getPicUrl();
                    System.out.println(new Gson().toJson(response.getSnbody().getAddNPic()));
                    image.setPicurl(picUrl);
                    SQLExecutor.update(image);
                }
            } catch (SuningApiException e) {
                e.printStackTrace();
            }

        }
        return picUrl;
    }



    public static void run(HtmlCache cache, String suningCate, String suningBrand) throws Exception {

        Document doc = Jsoup.parse(cache.getHtml());

        // border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;

        // 4级别分类
        Elements crumbs = doc.select("div.crumbs > div.wrapper > a");
        int index = crumbs.size() > 4 ? 4 : crumbs.size()-1;
        Element cate = crumbs.get(index);
        System.out.println(cate.attr("href") + ": " + cate.text());

        System.out.println("类目: " + StringUtils.getCode(cate.attr("href")));

        Element proDetailCon = doc.selectFirst("div.proDetailCon");

        // 标题前 品牌
        String brand1 =  proDetailCon.selectFirst("h3 > span > a").html();
        System.out.println(brand1);

        // 标题
        System.out.println(proDetailCon.selectFirst("h3 > a").html());

        String name = proDetailCon.selectFirst("h3 > a").text();

        Elements fonts = proDetailCon.select("div font");
        /**
         * 订 货 号：5W8061
         * 品   牌：霍尼韦尔 Honeywell
         * 制造商型号： SHSL00202-42
         * 包装内件数：1双
         * 预计发货日： 停止销售
         */
        String code = fonts.get(0).text();
        String brand = fonts.get(1).text();
        String model = fonts.get(2).text();
        String number = fonts.get(3).text();
        String fahuori = fonts.get(4).text();

        System.out.println("订 货 号： " + code);
        System.out.println("品   牌： " + brand + " - " + StringUtils.getCode(fonts.get(1).selectFirst("a").attr("href")));
        System.out.println("制造商型号： " + model);
        System.out.println("包装内件数： " + number);
        System.out.println("预计发货日： " + fahuori);



        String sellingPoint = proDetailCon.select("h4  span").last().text().replace("固安捷", "帷易胜");
        System.out.println("子标题(卖点):" + sellingPoint);
        System.out.println("");


        Element tableDiv = doc.selectFirst("div.tableDiv");
        //System.out.println(tableDiv.text());

        Element leftTable2 = tableDiv.selectFirst("div.leftTable2");
        for (Element trsku2 : leftTable2.select("tr.trsku2")) {

            Element selected = trsku2.selectFirst("td[name='tdItemNo'] > span.dweight");
            System.out.println("selected: " +selected);
            if ( selected != null ) {
                System.out.println("订货号： " +  trsku2.child(0).attr("title"));
                System.out.println("制造商型号： " +  trsku2.child(1).attr("title"));
                break;
            }
        }

        Element rightTable1 = tableDiv.selectFirst("div#rightTable1");

        List<String> columnName = new ArrayList<>();
        Element pxTR = rightTable1.selectFirst("tr.pxTR");
        for (Element element : pxTR.children()) {
            String tname = element.attr("title");
            if ( tname==null || tname.trim().length()==0 ) {
                tname = element.text();
            }
            columnName.add(tname);
            System.out.println("属性: " + tname);

        }

        Element rightTable2 = tableDiv.selectFirst("div#rightTable2");
        Elements trskus = rightTable2.select("tr.trsku2");

        Map<String, String> columnValues = new HashMap<>();

        if ( trskus.size() > 1 ) {
            for (Element trsku : trskus) {
                System.out.println(trsku);
                Element selected = trsku.selectFirst("td > a > span.dweight");
                if ( selected!=null ) {

                    Elements tds = trsku.children();
                    for (int i = 0; i < tds.size(); i++) {
                        Element td = tds.get(i);

                        String tvalue = td.attr("title");
                        if ( tvalue==null || tvalue.trim().length()==0 ) {
                            tvalue = td.text();
                        }

                        System.out.println(columnName.get(i) + ": " + tvalue);
                        columnValues.put(columnName.get(i), tvalue);
                    }
                    System.out.println("================");
                    break;
                }
            }
        } else {
            for (Element trsku : trskus) {
                System.out.println(trsku);
                Element selected = trsku.selectFirst("td > span.dweight");
                if ( selected!=null ) {

                    Elements tds = trsku.children();
                    for (int i = 0; i < tds.size(); i++) {
                        Element td = tds.get(i);

                        String tvalue = td.attr("title");
                        if ( tvalue==null || tvalue.trim().length()==0 ) {
                            tvalue = td.text();
                        }

                        System.out.println(columnName.get(i) + ": " + tvalue);
                        columnValues.put(columnName.get(i), tvalue);
                    }
                    System.out.println("================");
                    break;
                }
            }

        }


        String title = title(name, brand1, brand, model, number);
        System.out.println("[标题: " + title.length() + "]" + title);
        System.out.println("");

        // 小图片
        List<String> picUrls = new ArrayList<>();
        Elements imgs = doc.select("div.xiaotu > div.xtu > dl > dd > img");
        for (Element img : imgs) {
            String  src = img.attr("src");
            if ( src!=null && src.startsWith("//") ) {
                src = "https:" + src;
            }


           src = src.replace("product_images_new/350/", "product_images_new/800/");

            if ( src!=null && src.trim().length() > 0 ) {
                picUrls.add(src);
            }
            System.out.println(src);
        }

        Map<String, String> imageMap = new HashMap<>();
        for (String picUrl : picUrls) {
            String picSuningUrl = uploadImage(picUrl);
            imageMap.put(picUrl, picSuningUrl);
        }

        if ( picUrls.size() < 5 ) {
            int diff = 5 - picUrls.size();
            for (int i = 0; i < diff; i++) {

                ImageCache image = ImageLoader.loadIamge(picUrls.get(0));

                BufferedImage image1 = ImageIO.read(new File(ImageLoader.PICS_DIR + File.separator + image.getFilepath()));


                int x = new Random().nextInt(image1.getWidth());
                int y = new Random().nextInt(image1.getHeight());


                image1.setRGB(x, y, new Color(i, i, i, 72).getRGB());
                String file = "pic_" + i + ".jpg";

                File outputfile = new File(ImageLoader.PICS_DIR + File.separator + file);
                ImageIO.write(image1, "jpg", outputfile);




                String picUrl = null;
                {
                    String localFilePath = outputfile.getAbsolutePath();
                    NPicAddRequest request = new NPicAddRequest();
                    request.setPicFileData(localFilePath);
                    try {
                        NPicAddResponse response = client.excuteMultiPart(request);
                        System.out.println("ApplyAddRequest :" + response.getBody());
                        SuningResponse.SnError error = response.getSnerror();
                        if ( error!=null ) {
                            System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                        } else {
                            picUrl = response.getSnbody().getAddNPic().getPicUrl();
                            System.out.println(new Gson().toJson(response.getSnbody().getAddNPic()));
                            image.setPicurl(picUrl);
                            SQLExecutor.update(image);
                        }
                    } catch (SuningApiException e) {
                        e.printStackTrace();
                    }

                }
                imageMap.put(file, picUrl);

                picUrls.add(file);
            }
        }



        Element price = doc.selectFirst("div.price");
        Element bSalePrice = doc.selectFirst("b#bSalePrice");

        bSalePrice.remove();
        float _price = Float.parseFloat(bSalePrice.text().replace(",", "").replace("¥", ""));
        String unit = price.text().replace("价格:", "").trim();
        String unit1 = unit ;
        if ( unit.contains("/") ) {
            unit1 = unit.split("/")[1];
        }



        // 详情
        Element proDetailTit = doc.selectFirst("div.proDetailTit").nextElementSibling().child(0);


        List<String> detailUrls = new ArrayList<>();
        Map<String, String> detailImageMap = new HashMap<>();
        Elements detailImgs = doc.selectFirst("div.proDetailTit").nextElementSibling().select("img");
        if ( detailImgs!=null && detailImgs.size()>0 ) {

            for (Element img : detailImgs) {
                String  src = img.attr("src");
                if ( src!=null && src.startsWith("//") ) {
                    src = "https:" + src;
                }

                if ( src!=null && src.trim().length() > 0 ) {
                    detailUrls.add(src);
                }
                System.out.println(src);
            }

            for (String picUrl : detailUrls) {
                String picSuningUrl = uploadImage(picUrl);
                detailImageMap.put(picUrl, picSuningUrl);
            }
        }


        System.out.println(proDetailTit.outerHtml());

        StringBuilder builder = new StringBuilder();


        if ( _price < 100 ) {
            builder.append("<div class=\"box\">");


            builder.append("<div style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\">" +
                    "<span>商品起定量</span><span style=\"color:red;\">(请按起订量拍，否则无法发货)</span></div>");


            builder.append(" <span style=\"color:red;\">起订量： ").append( (int)Math.ceil(100/_price)).append(unit1).append("</span><br>");

            if ( number.contains(unit1) ) {
                builder.append(" 包装数量： ").append(number).append("<br>");
            } else {
                builder.append(" 包装数量： ").append(number).append("/").append(unit1).append("<br>");
            }

            builder.append("</div>");
        }

        builder.append("<div class=\"box\">");


        builder.append("<div style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品规格</div>");


        builder.append("•").append("制造商型号： ").append(model).append("<br>");
        for (String key : columnName) {
            builder.append("•").append(key).append(": ").append(columnValues.get(key));
            builder.append("<br>");
        }

        builder.append("<div  style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品描述</div>");
        builder.append(proDetailTit.html());

        if ( detailUrls.size() > 0 ) {
            builder.append("<br>");
            for (String picUrl : detailUrls) {
                String picSuningUrl = detailImageMap.get(picUrl);
                builder.append("<img alt=\"\" src=\"" + picSuningUrl + "\">");
                builder.append("<br>");
            }
        }

        builder.append("</div>");


        builder.append("<div  style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品图片</div>");
        for (String picUrl : picUrls) {
           if ( imageMap.containsKey(picUrl) && imageMap.get(picUrl)!=null ) {
               builder.append("<p><img alt=\"\" src=\"" +  imageMap.get(picUrl).trim() + "\" class=\"product\"></p>");
           }
        }
        builder.append("</div>");

        String introduction = Base64.encodeBase64String(builder.toString().replace("固安捷", "帷易胜").getBytes());

        System.out.println(introduction);


        List<Itemparameter> itemparameters =  SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER  where CATEGORYCODE = ?", Itemparameter.class, suningCate);
        for (Itemparameter parameter : itemparameters) {
            if ( parameter.getIsMust() !=null && "X".equalsIgnoreCase(parameter.getIsMust()) ) {
                System.out.println( "[" + parameter.getParaTemplateDesc() + "][" + parameter.getIsMust() + "]" + parameter.getParCode() + ": " + parameter.getParName() + " ==> " + parameter.getParType() + " >>>  " + parameter.getDataType());

                if (  parameter.getParOption()!=null &&  parameter.getParOption().size() > 0 ) {
                    for (Itemparameter.ParOption parOption : parameter.getParOption()) {
                        System.out.print("\t" + parOption.getParOptionCode() +  ": " +  parOption.getParOptionDesc() + "; ");
                    }
                    System.out.println("");
                }
            }
        }
//








        // ---------------------------------------------------------------


        // 类目 品牌 信息
        CatetoryBrandSelector selector = new SuningCatetoryBrandSelector();
        CatetoryBrand catetoryBrand = selector.selectCatetoryBrand();

        ApplyAddRequest request = new ApplyAddRequest();

        String suffix = "-测试";

        String itemCode = code + suffix; //  供应商商品编码
        String productName = title + suffix; // 大衣	商品名称
        String cmTitle = title + suffix; // 商品标题

        request.setCategoryCode(catetoryBrand.getCategoryCode());  // 类目编码
        request.setBrandCode(catetoryBrand.getBrandCode());       // 品牌编码



        request.setItemCode(itemCode); // 供应商商品编码
        request.setProductName(productName);
        request.setCmTitle(cmTitle); // 商品标题

        request.setSellingPoints(sellingPoint.length()>10?sellingPoint.substring(0, 10) : sellingPoint); // 商品卖点
        //request.setHighlightWordone( brand.length()>4?brand.substring(0, 4) : brand);
        //request.setHighlightWordtwo(model);
        //request.setHighlightWordthree(sellingPoint.length()>6?sellingPoint.substring(0, 6) : sellingPoint);

        /**
         * 商家商品介绍，UTF-8格式。将html内容的txt文本文件读取为字节数组,然后base64加密，去除空格回车后作为字段，传输时所涉及的图片不得使用外部url。允许写入CSS（禁止引用外部CSS）不支持JS。
         */
        request.setIntroduction(introduction); // 商家商品介绍 -- 好品质好商品


        List<ApplyAddRequest.Pars> parsList =new ArrayList<ApplyAddRequest.Pars>();


        boolean hasChild = false;
        for (Itemparameter parameter : itemparameters) {
            if ( parameter.getIsMust() !=null && "X".equalsIgnoreCase(parameter.getIsMust()) ) {

                if ( "通用参数模板".equalsIgnoreCase(parameter.getParaTemplateDesc()) ) {
                    hasChild = true;
                    continue;
                }
                ApplyAddRequest.Pars pars= new ApplyAddRequest.Pars();
                pars.setParCode(parameter.getParCode());

                if (  parameter.getParOption()!=null &&  parameter.getParOption().size() > 0 ) {
                    pars.setParValue(parameter.getParOption().get(0).getParOptionCode());
                }

                if (  "3".equalsIgnoreCase(parameter.getParType())  ) {
                    pars.setParValue("1");
                }


                boolean require = true;
                switch ( parameter.getParCode() ) {
                    case "cmModel": // 商品型号
                        pars.setParValue(model);
                        break;
                    case "country": // 国家
                        pars.setParValue("cn");
                        break;
                    case "taxCateg": // 商品税分类
                        pars.setParValue("E"); // JE--13% 进项税，中国
                        break;
                    case "G00000": // 色卡
                    case "G00001": // 颜色
                        require = false;
                        break;
                }
                parsList.add(pars);
            }
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


        // supplierImgUrl 商家商品图片 urlA~urlE
        List<ApplyAddRequest.SupplierImgUrl> supplierImgUrls = new ArrayList<>();
        ApplyAddRequest.SupplierImgUrl supplierImgUrl = new ApplyAddRequest.SupplierImgUrl();

        int i = 1;
        for (String picUrl : picUrls) {
            if ( imageMap.containsKey(picUrl) && imageMap.get(picUrl)!=null ) {
                String image = imageMap.get(picUrl).trim();
                switch ( i ){
                    case 1:
                        supplierImgUrl.setUrlA(image);
                        break;
                    case 2:
                        supplierImgUrl.setUrlB(image);
                        break;
                    case 3:
                        supplierImgUrl.setUrlC(image);
                        break;
                    case 4:
                        supplierImgUrl.setUrlD(image);
                        break;
                    case 5:
                        supplierImgUrl.setUrlE(image);
                        break;
                }
                i++;
            }
        }
        supplierImgUrls.add(supplierImgUrl);
        request.setSupplierImgUrl(supplierImgUrls);


        // 含有通子码 需要添加子型号
        if ( hasChild ) {
            List<ApplyAddRequest.ChildItem> childItems = new ArrayList<>();

            ApplyAddRequest.ChildItem childItem = new ApplyAddRequest.ChildItem();

            //childItem.setItemCodeX("ddfdf");
            List<ApplyAddRequest.ParsX> parsX = new ArrayList<>();



            for (Itemparameter parameter : itemparameters) {
                if ( parameter.getIsMust() !=null && "X".equalsIgnoreCase(parameter.getIsMust()) ) {
                    if ( !"通用参数模板".equalsIgnoreCase(parameter.getParaTemplateDesc()) ) {
                        continue;
                    }

                    ApplyAddRequest.ParsX parx = new ApplyAddRequest.ParsX();
                    parx.setParCodeX(parameter.getParCode());
                    if (  parameter.getParOption()!=null &&  parameter.getParOption().size() > 0 ) {
                        parx.setParValueX(parameter.getParOption().get(0).getParOptionCode());
                        if (parx.getParValueX()==null || parx.getParValueX().trim().length()==0 ) {
                            parx.setParValueX(parameter.getParOption().get(0).getParOptionDesc());
                        }

                    }

                    if (  "3".equalsIgnoreCase(parameter.getParType())  ) {
                        parx.setParValueX("1");
                    }

                    switch ( parameter.getParCode() ) {
                        case "G00021": //
                            parx.setParValueX("80米");
                            break;
                        case "G00001": // 国家
                            parx.setParValueX("蓝色");
                            break;
                    }


                    parsX.add(parx);
                }
            }

            childItem.setParsX(parsX);
            childItems.add(childItem);
            request.setChildItem(childItems);
        }


        //request.setSupplierImg1Url("https://static.grainger.cn/product_images_new/800/2A1/2013121616015256969.JPG");

        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        System.out.println(request.getResParams());




        try {


            ApplyAddResponse response = client.excute(request);
            System.out.println("ApplyAddRequest :" + response.getBody());

            SuningResponse.SnError error = response.getSnerror();
            if ( error!=null ) {
                System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
            } else {
                System.out.println(new Gson().toJson(response.getSnbody().getAddApply()));
            }



        } catch (SuningApiException e) {
            e.printStackTrace();
        }



    }
}
