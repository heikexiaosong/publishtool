package com.gavel.shelves;

import com.gavel.HttpUtils;
import com.gavel.config.APPConfig;
import com.gavel.crawler.HtmlPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.ImageCache;
import com.gavel.entity.Item;
import com.gavel.entity.ShelvesItem;
import com.gavel.grainger.StringUtils;
import com.gavel.utils.ImageLoader;
import com.gavel.utils.MD5Utils;
import com.google.gson.Gson;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.NPicAddRequest;
import com.suning.api.entity.item.NPicAddResponse;
import com.suning.api.exception.SuningApiException;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

public class ShelvesItemParser {

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

    public static ShelvesItem parse(Item item) throws Exception {

        if ( item==null || item.getUrl()==null || item.getUrl().trim().length()==0 ) {
            throw new Exception("item 或者 产品URL 不能为空");
        }

        HtmlCache htmlCache = HtmlPageLoader.getInstance().loadHtmlPage(item.getUrl(), true);
        if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
            throw new Exception("[Item: " + item.getUrl() +"]html获取失败.");
        }

        Document doc = Jsoup.parse(htmlCache.getHtml());

        Element err = doc.selectFirst("div.err-notice");
        if ( err!=null ) {
            throw new Exception("[URL: " + item.getUrl() + "]" + doc.title());
        }



        ShelvesItem shelvesItem = new ShelvesItem();

        shelvesItem.setSkuCode(item.getCode());

        // 4级类目 + 产品组ID + ID
        Elements elements = doc.select("div.crumbs  a");
        Element c4 = elements.get(4);
        Element c5 = elements.get(5);

        System.out.println(StringUtils.getCode(c4.attr("href")) + ": " + c4.text());
        shelvesItem.setCategoryCode(StringUtils.getCode(c4.attr("href")));
        shelvesItem.setCategoryname(c4.text());

        shelvesItem.setItemCode(item.getCode());
        shelvesItem.setProductName(c5.text());

        Element proDetailCon = doc.selectFirst("div.proDetailCon");

        Element h3 = proDetailCon.selectFirst(" > h3 ");
        h3.remove();

        Element brandCn = h3.selectFirst(" > span a");
        brandCn.remove();
        System.out.println("中文品牌: " + brandCn.text() + ": " + StringUtils.getCode(brandCn.attr("href")));

        Element title = h3.selectFirst(" > a");
        title.remove();
        System.out.println("标题: " + title.text());
        shelvesItem.setCmTitle(title.text());


        Element sellPoint = proDetailCon.select(" > h4 span").last();
        sellPoint.remove();
        shelvesItem.setSellingPoints(sellPoint.text());


        Element price = proDetailCon.selectFirst(" > div.price");
        price.remove();
        System.out.println("价格: " + price.text());

        Element priceEle =  (Element)price.childNodes().get(1);
        TextNode unitEle =  (TextNode)price.childNodes().get(2);

        float _price = Float.parseFloat(priceEle.text().replace(",", "").replace("¥", ""));
        String unit = unitEle.text().trim();
        String unit1 = unit ;
        if ( unit.contains("/") ) {
            unit1 = unit.split("/")[1];
        }

        System.out.println("Price: " + _price + "[" + unit + "]" + "[" + unit1 + "]");

        Elements attrs = proDetailCon.select(" > div font");
        attrs.remove();
        /**
         * 订 货 号：5W8061
         * 品   牌：霍尼韦尔 Honeywell
         * 制造商型号： SHSL00202-42
         * 包装内件数：1双
         * 预计发货日： 停止销售
         */

        System.out.println("\n属性: \n" + attrs.html());

        Element brandEle = attrs.get(1).selectFirst("a");
        shelvesItem.setBrandCode(StringUtils.getCode(brandEle.attr("href")));
        shelvesItem.setBrandname(brandEle.text());

        String model = attrs.get(2).text();
        String number = attrs.get(3).text();
        String fahuori = attrs.get(4).text();
        shelvesItem.setModel(model);

        System.out.println("制造商型号： " + model);
        System.out.println("包装内件数： " + number);
        System.out.println("预计发货日： " + fahuori);


        String _title = title(title.text(), brandCn.text(), brandEle.text(), model, number);
        shelvesItem.setCmTitle(_title);


        // 规格数据表格
        Element tableDiv = doc.selectFirst("div.tableDiv");
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
                Element selected = trsku.selectFirst("td  span.dweight");
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

        return shelvesItem;
    }


    /**
     * 图片下载并上传
     *
     * @throws Exception
     */
    public static String uploadDetailImage(String url) throws Exception {

        ImageCache image = ImageLoader.loadOrginialIamge(url);
        if ( image==null || image.getFilepath()==null || image.getFilepath().trim().length()==0 ) {
            return null;
        }

        if ( image.getPicurl()!=null && image.getPicurl().trim().length() > 0 ) {
            return image.getPicurl();
        }

        String localFilePath = ImageLoader.PICS_DIR + File.separator + image.getFilepath();
        if ( !new File(localFilePath).exists() ) {
            return null;
        }

        String picUrl = null;
        {
            NPicAddRequest request = new NPicAddRequest();
            request.setPicFileData(localFilePath);
            try {
                NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
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

    /**
     * 图片下载并上传
     *
     * @throws Exception
     */
    public static String uploadImage(String url, int size) throws Exception {


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


            Mat src = Imgcodecs.imread(localFilePath, Imgcodecs.IMREAD_UNCHANGED);
            if ( src.width()!=800 || src.height()!=800 ) {
                Imgproc.resize(src, src, new Size(800, 800));
            }
            Imgproc.circle(src, new Point(src.width()-size, src.height()-size),  2, new Scalar(238, 238, 238));

            localFilePath = localFilePath.replace(".jpg", ".png");
            Imgcodecs.imwrite(localFilePath, src);

            NPicAddRequest request = new NPicAddRequest();
            request.setPicFileData(localFilePath);
            try {
                NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    picUrl = response.getSnbody().getAddNPic().getPicUrl();
                    image.setPicurl(picUrl);
                    SQLExecutor.update(image);
                }
            } catch (SuningApiException e) {
                e.printStackTrace();
            }

        }
        return picUrl;
    }

    /**
     * 图片只上传
     *
     * @throws Exception
     */
    public static String uploadImageWithoutDownload(String url, int size) throws Exception {


        String id = MD5Utils.md5Hex(url.trim());

        ImageCache image =  SQLExecutor.executeQueryBean("select * from image  where id = ? ", ImageCache.class, id);

        if ( image==null ) {
            String _image = url.replace("https://static.grainger.cn/", "").replace("/", File.separator).trim();
            if ( url.equalsIgnoreCase("https://www.grainger.cn/Content/images/hp_np.png") ) {
                _image = url.replace("https://www.grainger.cn/", "").replace("/", File.separator).trim();
            }
            image = new ImageCache();
            image.setId(id);
            image.setUrl(url.trim());
            image.setFilepath(_image);
            image.setUpdatetime(Calendar.getInstance().getTime());
            try {
                SQLExecutor.insert(image);
            } catch (Exception e) {

            }
        }

        if ( image.getPicurl()!=null && image.getPicurl().trim().length() > 0 ) {
            return image.getPicurl();
        }

        String picUrl = null;
        String localFilePath = ImageLoader.PICS_COMPLETE_DIR + File.separator + image.getFilepath();
        if ( !new File(localFilePath).exists() ) {
            System.out.println("[" +  localFilePath + "]文件不存在.");
            return picUrl;
        }


        Mat src = Imgcodecs.imread(localFilePath, Imgcodecs.IMREAD_UNCHANGED);

        Imgproc.circle(src, new Point(src.width()-size, src.height()-size),  2, new Scalar(238, 238, 238));

        localFilePath = ImageLoader.PICS_TEMP_DIR + File.separator  + size + File.separator + image.getFilepath();

        File tempfile = new File(localFilePath);
        if ( !tempfile.getParentFile().exists() ) {
            tempfile.getParentFile().mkdirs();
        }

        System.out.println(localFilePath);

        localFilePath = localFilePath.replace(".jpg", ".png");
        Imgcodecs.imwrite(localFilePath, src);
        {

            NPicAddRequest request = new NPicAddRequest();
            request.setPicFileData(localFilePath);
            try {
                NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    picUrl = response.getSnbody().getAddNPic().getPicUrl();
                    image.setPicurl(picUrl);
                    SQLExecutor.update(image);
                }
            } catch (SuningApiException e) {
                e.printStackTrace();
            }

        }
        return picUrl;
    }


    public static List<String> getProductImages(String skuCode){
        List<String> images = new ArrayList<>();
        try {
            HtmlCache htmlCache = HtmlPageLoader.getInstance().loadGraingerPage(skuCode, true);
            if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
                throw new Exception("[Sku: " + skuCode +"]Html获取失败.");
            }


            Document doc = Jsoup.parse(htmlCache.getHtml());

            Element err = doc.selectFirst("div.err-notice");
            if ( err!=null ) {
                throw new Exception("[URL: " + doc.location() + "]" + doc.title());
            }

            // 小图片
            List<String> picUrls = new ArrayList<>();
            Elements imgs = doc.select("div.xiaotu > div.xtu > dl > dd > img");
            for (Element img : imgs) {
                String  src = img.attr("src");
                if (com.gavel.utils.StringUtils.isBlank(src) || "/Content/images/hp_np.png".equalsIgnoreCase(src) ) {
                    continue;
                }

                if ( src.startsWith("//") ) {
                    src = "https:" + src;
                }

                src = src.replace("product_images_new/350/", "product_images_new/800/");
                picUrls.add(src);
            }

            System.out.println("产品图片: " + picUrls.size());
            for (String picUrl : picUrls) {
               System.out.println("\t" + picUrl);
               try {
                   ImageCache image = ImageLoader.loadOrginialIamge(picUrl);
                   if ( image==null || com.gavel.utils.StringUtils.isBlank(image.getFilepath())) {
                       continue;
                   }

                   String localFilePath = ImageLoader.PICS_COMPLETE_DIR + File.separator + image.getFilepath();
                   if ( new File(localFilePath).exists() ) {
                       System.out.println("[" +  localFilePath + "]文件存在.");
                       continue;
                   }
                   images.add(ImageLoader.PICS_DIR + File.separator + image.getFilepath());
               } catch (Exception e) {

               }
            }
        } catch (Exception e) {

        }

        return images;
    }

    public static List<String> getImages(String skuCode, String defaultImage){
        List<String> images = new ArrayList<>();
        try {
            HtmlCache htmlCache = HtmlPageLoader.getInstance().loadGraingerPage(skuCode, true);
            if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
                throw new Exception("[Sku: " + skuCode +"]Html获取失败.");
            }

            Document doc = Jsoup.parse(htmlCache.getHtml());

            Element err = doc.selectFirst("div.err-notice");
            if ( err!=null ) {
                throw new Exception("[URL: " + doc.location() + "]" + doc.title());
            }

            // 小图片
            List<String> picUrls = new ArrayList<>();
            Elements imgs = doc.select("div.xiaotu > div.xtu > dl > dd > img");
            for (Element img : imgs) {
                String  src = img.attr("src");
                if (com.gavel.utils.StringUtils.isBlank(src) || "/Content/images/hp_np.png".equalsIgnoreCase(src) ) {
                    continue;
                }

                if ( src.startsWith("//") ) {
                    src = "https:" + src;
                }

                src = src.replace("product_images_new/350/", "product_images_new/800/");
                picUrls.add(src);
            }

            System.out.println("产品图片: " + picUrls.size());
            for (String picUrl : picUrls) {
                System.out.println(picUrl);
            }

            if ( picUrls.size() ==0 ) {
                String picSuningUrl =  uploadLocalImage(defaultImage);
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    images.add(picSuningUrl);
                }
                return images;
            }

            for (String picUrl : picUrls) {
                String picSuningUrl = uploadImageWithoutDownload(picUrl, images.size());
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    images.add(picSuningUrl);
                } else {
                    picSuningUrl = uploadImage(picUrl, images.size());
                    if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                        images.add(picSuningUrl);
                    }
                }
            }

            if ( images==null || images.size()==0) {
                String picSuningUrl =  uploadLocalImage(defaultImage);
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    images.add(picSuningUrl);
                }
                return images;
            }

            String pic1 = images.get(0);
            while ( images.size() < 5 ) {

                String localFilePath =  skuCode + "_" + images.size() + ".png";
                File imageFile = new File("images_tmp", localFilePath);
                if ( !imageFile.getParentFile().exists() ) {
                    imageFile.getParentFile().mkdirs();
                }

                if ( !imageFile.exists() ) {
                    HttpUtils.download(pic1, imageFile.getAbsolutePath());
                }

                System.out.println(localFilePath);
                Mat src = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);

                Imgproc.circle(src, new Point(src.width()-images.size(), src.height()-images.size()),  2, new Scalar(238, 238, 238));
                Imgcodecs.imwrite(imageFile.getAbsolutePath(), src);

                String picUrl = null;
                {
                    NPicAddRequest request = new NPicAddRequest();
                    request.setPicFileData(imageFile.getAbsolutePath());
                    try {
                        NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                        SuningResponse.SnError error = response.getSnerror();
                        if ( error!=null ) {
                            System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                        } else {
                            picUrl = response.getSnbody().getAddNPic().getPicUrl();
                            images.add(picUrl);
                        }
                    } catch (SuningApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    // 上传本地图片
    private static String uploadLocalImage(String url) throws Exception {
        if (com.gavel.utils.StringUtils.isBlank(url)) {
            return null;
        }

        String id = MD5Utils.md5Hex(url.trim());
        ImageCache image =  SQLExecutor.executeQueryBean("select * from image  where id = ? ", ImageCache.class, id);
        if ( image==null ) {
            image = new ImageCache();
            image.setId(MD5Utils.md5Hex(url.trim()));
            image.setUrl(url.trim());
            image.setFilepath(url.trim());
            try {
                SQLExecutor.insert(image);
            } catch (Exception e){

            }
        }

        if ( image.getPicurl()!=null && image.getPicurl().trim().length() > 0 ) {
            return image.getPicurl();
        }

        String picUrl = null;
        String localFilePath = url;
        if ( !new File(localFilePath).exists() ) {
            System.out.println("[" +  localFilePath + "]文件不存在.");
            return picUrl;
        }

        {

            NPicAddRequest request = new NPicAddRequest();
            request.setPicFileData(localFilePath);
            try {
                NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
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

    public static String buildIntroduction(ShelvesItem _item, int moq) throws Exception {

        return buildIntroduction(_item.getSkuCode(), moq);
    }


    public static String buildIntroduction(String skuCode, int moq) throws Exception {

        HtmlCache htmlCache = HtmlPageLoader.getInstance().loadGraingerPage(skuCode, true);
        if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
            throw new Exception("[Item: " + skuCode +"]html获取失败.");
        }

        Document doc = Jsoup.parse(htmlCache.getHtml());

        Element err = doc.selectFirst("div.err-notice");
        if ( err!=null ) {
            throw new Exception("[Sku: " + skuCode + "]" + doc.title());
        }


        Element proDetailCon = doc.selectFirst("div.proDetailCon");

        Element h3 = proDetailCon.selectFirst(" > h3 ");
        h3.remove();

        Element brandCn = h3.selectFirst(" > span a");
        brandCn.remove();
        //System.out.println("中文品牌: " + brandCn.text() + ": " + StringUtils.getCode(brandCn.attr("href")));

        Element title = h3.selectFirst(" > a");
        title.remove();
        //System.out.println("标题: " + title.text());


        Element price = proDetailCon.selectFirst(" > div.price");
        price.remove();
        //System.out.println("价格: " + price.text());

        Element priceEle =  (Element)price.childNodes().get(1);
        float _price = Float.parseFloat(priceEle.text().replace(",", "").replace("¥", ""));


        TextNode unitEle =  (TextNode)price.childNodes().get(2);
        String unit = unitEle.text().trim();
        if ( unit.contains("/") ) {
            unit = unit.split("/")[1];
        }

        System.out.println("Price: " + _price + "[" + unit + "]");


        /**
         * 订 货 号：5W8061
         * 品   牌：霍尼韦尔 Honeywell
         * 制造商型号： SHSL00202-42
         * 包装内件数：1双
         * 预计发货日： 停止销售
         */
        Elements attrs = proDetailCon.select(" > div font");
        attrs.remove();
        String model = attrs.get(2).text();
        String number = attrs.get(3).text();


        // 规格数据表格
        Element tableDiv = doc.selectFirst("div.tableDiv");

        Element rightTable1 = tableDiv.selectFirst("div#rightTable1");
        List<String> columnName = new ArrayList<>();
        Element pxTR = rightTable1.selectFirst("tr.pxTR");
        for (Element element : pxTR.children()) {
            String tname = element.attr("title");
            if ( tname==null || tname.trim().length()==0 ) {
                tname = element.text();
            }
            columnName.add(tname);
        }

        Element rightTable2 = tableDiv.selectFirst("div#rightTable2");
        Elements trskus = rightTable2.select("tr.trsku2");

        Map<String, String> columnValues = new HashMap<>();
        if ( trskus.size() > 1 ) {
            for (Element trsku : trskus) {
                Element selected = trsku.selectFirst("td  span.dweight");
                if ( selected!=null ) {

                    Elements tds = trsku.children();
                    for (int i = 0; i < tds.size(); i++) {
                        Element td = tds.get(i);

                        String tvalue = td.attr("title");
                        if ( tvalue==null || tvalue.trim().length()==0 ) {
                            tvalue = td.text();
                        }
                        columnValues.put(columnName.get(i), tvalue);
                    }
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


        // 产品描述
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
            }

            for (String picUrl : detailUrls) {
                String picSuningUrl = uploadDetailImage(picUrl);
                if (com.gavel.utils.StringUtils.isNotBlank(picSuningUrl)) {
                    detailImageMap.put(picUrl, picSuningUrl);
                }
            }
        }


        StringBuilder detail = new StringBuilder();
        if ( _price < moq ) {
            detail.append("<div class=\"box\">");
            detail.append("<div style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\">" +
                    "<span>商品起定量</span><span style=\"color:red;\">(请按起订量拍，否则无法发货)</span></div>");


            detail.append(" <span style=\"color:red;\">起订量： ").append( (int)Math.ceil(100/_price)).append(unit).append("</span><br>");

            if ( number.contains(unit) ) {
                detail.append(" 包装数量： ").append(number).append("<br>");
            } else {
                detail.append(" 包装数量： ").append(number).append("/").append(unit).append("<br>");
            }

            detail.append("</div>");
        }

        detail.append("<div class=\"box\">");


        detail.append("<div style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品规格</div>");


        detail.append("•").append("制造商型号： ").append(model).append("<br>");
        for (String key : columnName) {
            detail.append("•").append(key).append(": ").append(columnValues.get(key));
            detail.append("<br>");
        }

        detail.append("<div  style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品描述</div>");
        detail.append(proDetailTit.html());

        System.out.println("详情图片: " + detailImageMap.size());
        if ( detailUrls.size() > 0 ) {
            detail.append("<br>");
            for (String picUrl : detailUrls) {
                String picSuningUrl = detailImageMap.get(picUrl);
                if (com.gavel.utils.StringUtils.isNotBlank(picSuningUrl)) {
                    detail.append("<img alt=\"\" src=\"" + picSuningUrl + "\">");
                    detail.append("<br>");
                }
            }
        }
        detail.append("</div>");



        // 小图片
        List<String> picUrls = new ArrayList<>();
        Elements imgs = doc.select("div.xiaotu > div.xtu > dl > dd > img");
        for (Element img : imgs) {
            String  src = img.attr("src");
            if ( "/Content/images/hp_np.png".equalsIgnoreCase(src) ) {
                continue;
            }

            if ( src!=null && src.startsWith("//") ) {
                src = "https:" + src;
            }
            src = src.replace("product_images_new/350/", "product_images_new/800/");
            if ( com.gavel.utils.StringUtils.isBlank(src) ) {
                picUrls.add(src);
            }
        }

        Map<String, String> imageMap = new HashMap<>();
        for (String picUrl : picUrls) {
            System.out.println("\t" + picUrl);
            String picSuningUrl = uploadImageWithoutDownload(picUrl, 0);
            if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                imageMap.put(picUrl, picSuningUrl);
            } else {
                picSuningUrl = uploadImage(picUrl, 0);
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    imageMap.put(picUrl, picSuningUrl);
                }
            }

            System.out.println("\t ==> " + imageMap.get(picUrl));
        }


        detail.append("<div  style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品图片</div>");
        for (String picUrl : picUrls) {
            if ( imageMap.containsKey(picUrl) && com.gavel.utils.StringUtils.isNotBlank(imageMap.get(picUrl))) {
                detail.append("<p><img alt=\"\" src=\"" +  imageMap.get(picUrl).trim() + "\" class=\"product\"></p>");
            }
        }
        detail.append("</div>");

        System.out.println("Detail: " + detail.toString());

        return Base64.encodeBase64String(detail.toString().getBytes("UTF8"));
    }



    public static void main(String[] args) throws Exception {

        APPConfig.getInstance().getShopinfo();

        buildIntroduction("1M0412", 100);
    }

}
