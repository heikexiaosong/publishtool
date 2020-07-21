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
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.NPicAddRequest;
import com.suning.api.entity.item.NPicAddResponse;
import com.suning.api.exception.SuningApiException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
        if ( doc.title().equalsIgnoreCase("403 Forbidden") ) {
            SQLExecutor.delete(htmlCache);
            throw new Exception("[Item: " + item.getUrl() +"]html获取失败.");
        } else if ( doc.title().equalsIgnoreCase("Error") ) {
            SQLExecutor.delete(htmlCache);
            throw new Exception("[Item: " + item.getUrl() +"]html获取失败.");
        }


        Element err = doc.selectFirst("div.err-notice");
        if ( err!=null ) {
            SQLExecutor.delete(htmlCache);
            throw new Exception("[URL: " + item.getUrl() + "]" + doc.title());
        }

        ShelvesItem shelvesItem = new ShelvesItem();

        shelvesItem.setSkuCode(item.getCode());

        // 4级类目 + 产品组ID + ID
        Elements elements = doc.select("div.crumbs  a");
        Element c4 = elements.get(4);
        Element c5 = elements.get(5);

        shelvesItem.setCategoryCode(StringUtils.getCode(c4.attr("href")));
        shelvesItem.setCategoryname(c4.text());

        shelvesItem.setItemCode(item.getCode());
        shelvesItem.setProductName(c5.text());

        Element proDetailCon = doc.selectFirst("div.proDetailCon");

        Element h3 = proDetailCon.selectFirst(" > h3 ");
        h3.remove();

        Element brandCn = h3.selectFirst(" > span a");
        brandCn.remove();

        Element title = h3.selectFirst(" > a");
        title.remove();
        shelvesItem.setCmTitle(title.text());


        Element sellPoint = proDetailCon.select(" > h4 span").last();
        sellPoint.remove();
        shelvesItem.setSellingPoints(sellPoint.text());


        Element price = proDetailCon.selectFirst(" > div.price");
        price.remove();

        Elements attrs = proDetailCon.select(" > div font");
        attrs.remove();
        /**
         * 订 货 号：5W8061
         * 品   牌：霍尼韦尔 Honeywell
         * 制造商型号：SHSL00202-42
         * 包装内件数：1双
         * 预计发货日： 停止销售
         */
        Element brandEle = attrs.get(1).selectFirst("a");
        shelvesItem.setBrandCode(StringUtils.getCode(brandEle.attr("href")));
        shelvesItem.setBrandname(brandEle.text());

        String model = attrs.get(2).text();
        String number = attrs.get(3).text();
        shelvesItem.setModel(model);
        shelvesItem.setDelivery(attrs.get(4).text());


        String _title = title(title.text(), brandCn.text(), brandEle.text(), model, number);
        shelvesItem.setCmTitle(_title);

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
            int cnt = 0;
            while ( picUrl==null && cnt < 3 ) {
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

                if ( picUrl!=null && picUrl.trim().length() > 0) {
                    try {
                        HttpUtils.download(picUrl, "d:\\check.png");
                        File imageFile = new File("d:\\check.png");
                        if ( imageFile.exists() && imageFile.length() > 9999 ) {
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("[" + url + "]" + e.getMessage());
                        picUrl = null;
                    }
                }


                cnt++;
            }


        }
        return picUrl;
    }

    /**
     * 图片下载并上传
     *
     * @throws Exception
     */
    public static String uploadImage(String url, int size, BufferedImage logoImage) throws Exception {


        ImageCache image = ImageLoader.loadIamge(url);
        if ( image==null || image.getFilepath()==null || image.getFilepath().trim().length()==0 ) {
            return null;
        }

        if (  com.gavel.utils.StringUtils.isNotBlank(image.getPicurl()) && logoImage==null ) {
            return image.getPicurl();
        }

        String localFilePath = ImageLoader.PICS_DIR + File.separator + image.getFilepath();

        Mat src = Imgcodecs.imread(localFilePath, Imgcodecs.IMREAD_UNCHANGED);
        if ( src.width()<=50 || src.height()<=50 ) {
           return null;
        }

        if ( src.width()!=800 || src.height()!=800 ) {
            Imgproc.resize(src, src, new Size(800, 800));
        }
        Imgproc.circle(src, new Point(src.width()-size, src.height()-size),  2, new Scalar(238, 238, 238));

        localFilePath = localFilePath.replace(".jpg", ".png");
        Imgcodecs.imwrite(localFilePath, src);

        System.out.println("Local: " + localFilePath);


        String picUrl = null;
        if ( logoImage!=null ) {

            File picFile = new File(localFilePath);

            String outputFile =  picFile.getParent() + File.separator + "mask_" + picFile.getName();

            try {
                // ImageIO读取图片
                BufferedImage pic = ImageIO.read( new File(localFilePath));
                Thumbnails.of(pic)
                        // 设置图片大小
                        .size(pic.getWidth(), pic.getHeight())
                        // 加水印 参数：1.水印位置 2.水印图片 3.不透明度0.0-1.0
                        .watermark(Positions.TOP_LEFT, logoImage, 0.9f)
                        // 输出到文件
                        .toFile(outputFile);
                localFilePath = outputFile;
            } catch (IOException e) {
                e.printStackTrace();
            }



            NPicAddRequest request = new NPicAddRequest();
            request.setPicFileData(localFilePath);
            try {
                NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    System.out.println(response.getSnbody());
                    picUrl = response.getSnbody().getAddNPic().getPicUrl();
                }
            } catch (SuningApiException e) {
                e.printStackTrace();
            }


        } else {

            NPicAddRequest request = new NPicAddRequest();
            request.setPicFileData(localFilePath);
            try {
                NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    System.out.println(response.getSnbody());
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
    public static String uploadImageWithoutDownload(String url, int size, BufferedImage logoImage) throws Exception {


        String id = MD5Utils.md5Hex(url.trim());

        ImageCache image =  SQLExecutor.executeQueryBean("select * from image  where id = ? ", ImageCache.class, id);

        if ( image==null ) {
            String _image = ImageLoader.getPath(url);
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

        if (  com.gavel.utils.StringUtils.isNotBlank(image.getPicurl()) && logoImage==null ) {
            System.out.println("[" + image.getFilepath() + "]: " + image.getPicurl());
            return image.getPicurl();
        }

        String picUrl = null;
        String localFilePath = ImageLoader.PICS_COMPLETE_DIR + File.separator + image.getFilepath();
        if ( !new File(localFilePath).exists() ) {
            System.out.println("[" +  localFilePath + "]文件不存在.");
            return picUrl;
        }


        Mat src = Imgcodecs.imread(localFilePath, Imgcodecs.IMREAD_UNCHANGED);
        if ( src.width()<=50 || src.height()<=50 ) {
            return picUrl;
        }

        if ( src.width()!=800 || src.height()!=800 ) {
            Imgproc.resize(src, src, new Size(800, 800));
        }

        Imgproc.circle(src, new Point(src.width()-size, src.height()-size),  2, new Scalar(238, 238, 238));

        localFilePath = ImageLoader.PICS_TEMP_DIR + File.separator  + size + File.separator + image.getFilepath();

        File tempfile = new File(localFilePath);
        if ( !tempfile.getParentFile().exists() ) {
            tempfile.getParentFile().mkdirs();
        }

        localFilePath = localFilePath.replace(".jpg", ".png");
        Imgcodecs.imwrite(localFilePath, src);

        System.out.println("Local: " + localFilePath);


        if ( logoImage!=null ) {

            File picFile = new File(localFilePath);

            String outputFile =  picFile.getParent() + File.separator + "mask_" + picFile.getName();

            try {
                // ImageIO读取图片
                BufferedImage pic = ImageIO.read( new File(localFilePath));
                Thumbnails.of(pic)
                        // 设置图片大小
                        .size(pic.getWidth(), pic.getHeight())
                        // 加水印 参数：1.水印位置 2.水印图片 3.不透明度0.0-1.0
                        .watermark(Positions.TOP_LEFT, logoImage, 0.9f)
                        // 输出到文件
                        .toFile(outputFile);
                localFilePath = outputFile;
            } catch (IOException e) {
                e.printStackTrace();
            }

            NPicAddRequest request = new NPicAddRequest();
            request.setPicFileData(localFilePath);
            try {
                NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {
                    picUrl = response.getSnbody().getAddNPic().getPicUrl();
                }
            } catch (SuningApiException e) {
                e.printStackTrace();
            }
        } else {
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

    public static Map<String, String> parseAttrs(String skuCode) throws Exception {

        Map<String, String> attrs = new HashMap<>();

        Document doc = null;
        HtmlCache htmlCache = HtmlPageLoader.getInstance().loadGraingerPage(skuCode, true);
        if ( htmlCache!=null && htmlCache.getHtml()!=null && htmlCache.getHtml().trim().length()>0 ) {
            try {
                doc = Jsoup.parse(htmlCache.getHtml());
                if ( doc.selectFirst("div.err-notice")!=null ) {
                    doc = null;
                }
                if ( doc!=null && doc.title().equalsIgnoreCase("403 Forbidden") ) {
                    doc = null;
                }
                if ( doc!=null && doc.title().equalsIgnoreCase("Error") ) {
                    doc = null;
                }
            } catch (Exception e) {

            }
        }

        if ( doc == null ) {
            return attrs;
        }


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
                        attrs.put(columnName.get(i), tvalue);
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
                        attrs.put(columnName.get(i), tvalue);
                    }
                    System.out.println("================");
                    break;
                }
            }
        }

        return attrs;
    }

    public static class Pic {

        private final String url;

        private final boolean iscreate ;

        public Pic(String url, boolean iscreate) {
            this.url = url;
            this.iscreate = iscreate;
        }

        public String getUrl() {
            return url;
        }

        public boolean isIscreate() {
            return iscreate;
        }
    }


    public static List<ShelvesItemParser.Pic> getImages(String skuCode, List<String> picUrls, String defaultImage, BufferedImage logoImage){
        List<Pic> images = new ArrayList<>();
        try {
            System.out.println("产品主图: " + picUrls.size());
            for (String picUrl : picUrls) {
                System.out.println(picUrl);
            }

            if ( picUrls.size() ==0 ) {
                String picSuningUrl =  uploadLocalImage(defaultImage);
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    images.add(new Pic(picSuningUrl, true));
                }
                return images;
            }

            for (String picUrl : picUrls) {
                String picSuningUrl = uploadImageWithoutDownload(picUrl, images.size(), logoImage);
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    images.add(new Pic(picSuningUrl, false));
                } else {
                    picSuningUrl = uploadImage(picUrl, images.size(), logoImage);
                    if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                        images.add(new Pic(picSuningUrl, false));
                    }
                }
                if ( images.size() >= 5 ) {
                    break;
                }
            }

            if ( images==null || images.size()==0) {
                String picSuningUrl =  uploadLocalImage(defaultImage);
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    images.add(new Pic(picSuningUrl, true));
                }
                return images;
            }

            String pic1 = images.get(0).getUrl();
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
                if ( src.width()!=800 || src.height()!=800 ) {
                    Imgproc.resize(src, src, new Size(800, 800));
                }
                Imgproc.putText(src, Integer.toString(images.size()), new Point(src.width()-20, src.height()-10), 0, 1.0, new Scalar(220,220,220));
                //Imgproc.circle(src, new Point(src.width()-images.size(), src.height()-images.size()),  2, new Scalar(238, 238, 238));
                Imgcodecs.imwrite(imageFile.getAbsolutePath(), src);


                String picFile = imageFile.getAbsolutePath();
                if ( logoImage!=null ) {
                    String outputFile =  imageFile.getParent() + File.separator + "mask_" + imageFile.getName();
                    try {
                        // ImageIO读取图片
                        BufferedImage pic = ImageIO.read( new File(localFilePath));
                        Thumbnails.of(pic)
                                // 设置图片大小
                                .size(pic.getWidth(), pic.getHeight())
                                // 加水印 参数：1.水印位置 2.水印图片 3.不透明度0.0-1.0
                                .watermark(Positions.TOP_LEFT, logoImage, 0.9f)
                                // 输出到文件
                                .toFile(outputFile);
                        picFile = outputFile;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                String picUrl = null;
                {
                    NPicAddRequest request = new NPicAddRequest();
                    request.setPicFileData(picFile);
                    try {
                        NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                        SuningResponse.SnError error = response.getSnerror();
                        if ( error!=null ) {
                            System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                        } else {
                            picUrl = response.getSnbody().getAddNPic().getPicUrl();
                            images.add(new Pic(picUrl, true));
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


    public static List<ShelvesItemParser.Pic> getImages(String skuCode, String defaultImage, BufferedImage logoImage){
        List<Pic> images = new ArrayList<>();
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
                    images.add(new Pic(picSuningUrl, true));
                }
                return images;
            }

            for (String picUrl : picUrls) {
                String picSuningUrl = uploadImageWithoutDownload(picUrl, images.size(), logoImage);
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    images.add(new Pic(picSuningUrl, false));
                } else {
                    picSuningUrl = uploadImage(picUrl, images.size(), logoImage);
                    if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                        images.add(new Pic(picSuningUrl, false));
                    }
                }
            }

            if ( images==null || images.size()==0) {
                String picSuningUrl =  uploadLocalImage(defaultImage);
                if ( picSuningUrl!=null && picSuningUrl.trim().length() > 0 ) {
                    images.add(new Pic(picSuningUrl, true));
                }
                return images;
            }

            String pic1 = images.get(0).getUrl();
            while ( images.size() < 5 ) {

                String localFilePath =  skuCode + "_" + images.size() + ".png";
                File imageFile = new File("images_tmp" + File.separator + skuCode, localFilePath);
                if ( !imageFile.getParentFile().exists() ) {
                    imageFile.getParentFile().mkdirs();
                }

                if ( imageFile.exists() ) {
                    imageFile.delete();
                }

                String picPath = loadLocalImageFile(pic1);
                System.out.println("[" + (images.size()+1) + "]Load Local: " + picPath);
                if ( picPath==null ) {
                    continue;
                }
                Files.copy(new File(picPath), imageFile);

                Mat src = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
                if ( src.width()!=800 || src.height()!=800 ) {
                    Imgproc.resize(src, src, new Size(800, 800));
                }
                Imgproc.putText(src, Integer.toString(images.size()), new Point(src.width()-20, src.height()-10), 0, 1.0, new Scalar(220,220,220));
                //Imgproc.circle(src, new Point(src.width()-images.size(), src.height()-images.size()),  2, new Scalar(238, 238, 238));
                Imgcodecs.imwrite(imageFile.getAbsolutePath(), src);


                String picFile = imageFile.getAbsolutePath();
                if ( logoImage!=null ) {
                    String outputFile =  imageFile.getParent() + File.separator + "mask_" + imageFile.getName();
                    try {
                        // ImageIO读取图片
                        BufferedImage pic = ImageIO.read( new File(localFilePath));
                        Thumbnails.of(pic)
                                // 设置图片大小
                                .size(pic.getWidth(), pic.getHeight())
                                // 加水印 参数：1.水印位置 2.水印图片 3.不透明度0.0-1.0
                                .watermark(Positions.TOP_LEFT, logoImage, 0.9f)
                                // 输出到文件
                                .toFile(outputFile);
                        picFile = outputFile;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                String picUrl = null;
                {
                    NPicAddRequest request = new NPicAddRequest();
                    request.setPicFileData(picFile);
                    try {
                        NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
                        SuningResponse.SnError error = response.getSnerror();
                        if ( error!=null ) {
                            System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                        } else {
                            picUrl = response.getSnbody().getAddNPic().getPicUrl();
                            images.add(new Pic(picUrl, true));
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
            System.out.println("[Local]" + image.getFilepath());
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

    public static String buildIntroduction(ShelvesItem _item, int moq, List<ShelvesItemParser.Pic> images) throws Exception {

        return buildIntroduction(_item.getSkuCode(), moq, images);
    }


    public static String buildIntroduction(String skuCode, int moq, List<ShelvesItemParser.Pic> images) throws Exception {

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



        List<String> picUrls = new ArrayList<>();
        if ( images!=null && images.size() > 0 ) {
            for (Pic image : images) {
                if ( !image.isIscreate() &&  com.gavel.utils.StringUtils.isNotBlank(image.getUrl()) ){
                    picUrls.add(image.getUrl());
                }
            }

        }


        detail.append("<div  style=\"border-bottom:1px solid #e8e8e8!important;padding-left:10px;position:relative;font-size:14px;color:#333;font-weight:bold;margin-bottom:1px;height: 30px; line-height: 30px; background-color: #f5f5f5;\"><span></span>产品图片</div>");
        if ( picUrls.size() > 0 ) {
            for (String picUrl : picUrls) {
                detail.append("<p><img alt=\"\" src=\"" + picUrl + "\" class=\"product\"></p>");
            }
        }
        detail.append("<p><img alt=\"\" src=\"https://uimgproxy.suning.cn/uimg1/sop/commodity/MhdqxYCAnkWz57dhaZS4PQ.jpg\" class=\"product\"></p>");
        detail.append("</div>");



        System.out.println("Detail: " + detail.toString());

        return Base64.encodeBase64String(detail.toString().getBytes("UTF8"));
    }




    /**
     * 获取本地图片文件路径
     *
     * @param url
     * @return
     */
    private static String loadLocalImageFile(String url) {
        if ( url==null || url.trim().length()==0 ) {
            return null;
        }

        String image = url.replace("http://uimgproxy.suning.cn", "").replace("/", File.separator).trim();

        File imageFile = new File(ImageLoader.PICS_COMPLETE_DIR + File.separator + image);
        if (  imageFile.exists() ) {
            return imageFile.getAbsolutePath();
        }

        imageFile = new File(ImageLoader.PICS_DIR + File.separator + image);
        if (  imageFile.exists() ) {
            return imageFile.getAbsolutePath();
        }

        if ( !imageFile.getParentFile().exists() ) {
            imageFile.getParentFile().mkdirs();
        }

        try {
            HttpUtils.download(url, imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            System.out.println("[" + url + "]" + e.getMessage());
        }

        return null;
    }

    public static void main(String[] args) throws Exception {

        Mat src = Imgcodecs.imread("e:\\_A1hmFHelzvbhzjZXvssoQ.png", Imgcodecs.IMREAD_UNCHANGED);

        System.out.println(loadLocalImageFile("http://uimgproxy.suning.cn/uimg1/sop/commodity/ehw3ZPygWjnqvGu34WO5YA.png"));
    }


}
