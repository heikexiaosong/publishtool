package com.gavel.jd;

import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.ImageInfo;
import com.gavel.entity.ShelvesItem;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDSkuParser {

    private static final Pattern DETAIL_IMAGE = Pattern.compile("background-image:url\\(([^)]*)");
    private static final Pattern PIC_IMAGE = Pattern.compile("360buyimg.com/n(\\d*)/");
    private static final Pattern PIC_IMAGE_JFS = Pattern.compile("n12/(s.*_)jfs/");


    private final ShelvesItem item;

    private final String html;

    private final Document doc;

    public JDSkuParser(ShelvesItem item, String html) {
        this.item = item;
        this.html = html;
        Document _doc = null;
        try {
            _doc = Jsoup.parse(html);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.doc = _doc;
        }
    }


    public List<ImageInfo> loadImages(String picDir) {
        List<ImageInfo> imageInfos = loadImages(item.getId(), "M");
        if ( imageInfos==null || imageInfos.size() == 0 && doc!=null) {

            File dir = new File(picDir);
            if ( !dir.exists() ) {
                dir.mkdirs();
            }

            Elements imgs = doc.select("div#spec-list li>img");
            for (int i1 = 0; i1 < imgs.size(); i1++) {
                Element img = imgs.get(i1);

                String text = img.attr("src");
                if ( text.startsWith("//") ) {
                    text = "https:" + text;
                }
                Matcher mat = PIC_IMAGE.matcher(text);
                if (mat.find()){
                    text = text.substring(0,  mat.start()) + "360buyimg.com/n12/" + text.substring(mat.end());
                }
                mat = PIC_IMAGE_JFS.matcher(text);
                if (mat.find()){
                    text = text.substring(0, 32) + "jfs/" + text.substring(mat.end());
                }
                System.out.println("主图: " +  text);


                try {
                    String id = MD5Utils.md5Hex(item.getId() + "_M_" + i1);
                    ImageInfo exist = SQLExecutor.executeQueryBean("select * from ITEM_IMAGE where ID = ? ", ImageInfo.class, item.getId());
                    if ( exist==null ) {
                        exist = new ImageInfo();
                        exist.setId(id);
                        SQLExecutor.insert(exist);
                    }

                    boolean image_exist = false;
                    if ( exist!=null && StringUtils.isNotBlank(exist.getFilepath()) ) {
                        File image = new File(exist.getFilepath());
                        if ( image.exists() && image.length() > 999 ) {
                            // 图片存在
                            image_exist  = true;
                        }
                    }

                    if ( !image_exist ) {
                        String imageFileName =  item.getSkuCode() + "_" + (i1+1) + ".jpg";
                        File imageFile = new File(dir, imageFileName);

                        int cnt = 0;
                        while ( cnt++ < 3 ) {
                            try {
                                HttpUtils.download(text, imageFile.getAbsolutePath());

                                Mat src = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
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

                                Imgproc.putText(src, Integer.toString(i1+1), new Point(src.width()-30, src.height()-10), 0, 1.0, scalar);
                                Imgcodecs.imwrite(imageFile.getAbsolutePath(), src);

                                exist.setRefid(item.getId());
                                exist.setCode(item.getSkuCode());
                                exist.setXh(i1+1);
                                exist.setType("M");
                                exist.setPicurl(text);
                                exist.setFilepath(imageFile.getAbsolutePath());

                                SQLExecutor.update(exist);

                                imageInfos.add(exist);
                                break;
                            } catch (Exception e) {
                                System.out.println("图片下载失败: " + e.getMessage() + " ==> " + text);
                                Thread.sleep(1000);
                            }
                        }

                    }

                } catch (Exception e) {

                }
            }
            System.out.println("Images: " + imgs.size());

        }

        return imageInfos;
    }


    // 详情图
    public List<ImageInfo> loadDetailImages(String picDir) {
        List<ImageInfo> imageInfos = loadImages(item.getId(), "D");
        if ( imageInfos==null || imageInfos.size() == 0  && doc!=null) {

            File dir = new File(picDir);
            if ( !dir.exists() ) {
                dir.mkdirs();
            }

            int i1 = 1;
            Element detailcontent = doc.selectFirst("div#detail div#J-detail-content style");
            if ( detailcontent!=null ) {
                Matcher mat = DETAIL_IMAGE.matcher(detailcontent.html());
                while(mat.find()){
                    String text = mat.group(1);
                    if ( text.startsWith("(") ) {
                        text = text.substring(1);
                    }
                    if ( text.endsWith(")") ) {
                        text = text.substring(0, text.length()-1);
                    }
                    if ( text.startsWith("\"") ) {
                        text = text.substring(1);
                    }
                    if ( text.endsWith("\"") ) {
                        text = text.substring(0, text.length()-1);
                    }
                    if ( text.startsWith("//") ) {
                        text = "https:" + text;
                    }
                    System.out.println("style image: " + text);

                    if ( StringUtils.isBlank(text) ) {
                        continue;
                    }


                    try {
                        String id = MD5Utils.md5Hex(item.getId() + "_D_" + i1);
                        ImageInfo exist = SQLExecutor.executeQueryBean("select * from ITEM_IMAGE where ID = ? ", ImageInfo.class, id);
                        if ( exist==null ) {
                            exist = new ImageInfo();
                            exist.setId(id);
                            SQLExecutor.insert(exist);
                        }

                        boolean image_exist = false;
                        if ( exist!=null && StringUtils.isNotBlank(exist.getFilepath()) ) {
                            File image = new File(exist.getFilepath());
                            if ( image.exists() && image.length() > 999 ) {
                                // 图片存在
                                image_exist  = true;
                            }
                        }

                        if ( !image_exist ) {
                            String imageFileName =  item.getSkuCode() + "_" + i1 + "_detail" + ".jpg";
                            File imageFile = new File(dir, imageFileName);

                            int cnt = 0;
                            while ( cnt++ < 3 ) {
                                try {
                                    HttpUtils.download(text, imageFile.getAbsolutePath());

                                    exist.setRefid(item.getId());
                                    exist.setCode(item.getSkuCode());
                                    exist.setXh(i1);
                                    exist.setType("D");
                                    exist.setPicurl(text);
                                    exist.setFilepath(imageFile.getAbsolutePath());

                                    SQLExecutor.update(exist);

                                    imageInfos.add(exist);

                                    break;
                                } catch (Exception e) {
                                    System.out.println("图片下载失败: " + e.getMessage() + " ==> " + text);
                                    Thread.sleep(1000);
                                }
                            }
                        }

                    } catch (Exception e) {

                    }

                    i1++;
                }
            }

            Elements detailImgs = doc.select("div#detail div#J-detail-content img");
            if ( detailImgs!=null ) {
                for (Element detailImg : detailImgs) {

                    String src = detailImg.attr("src");
                    if ( src==null || src.trim().length()==0 ||  src.endsWith("blank.gif") ) {
                        src = detailImg.attr("data-lazyload");
                    }
                    if ( src.startsWith("//") ) {
                        src = "https:" + src;
                    }
                    System.out.println("J-detail-content img: " + src);

                    try {
                        String id = MD5Utils.md5Hex(item.getId() + "_D_" + i1);
                        ImageInfo exist = SQLExecutor.executeQueryBean("select * from ITEM_IMAGE where ID = ? ", ImageInfo.class, id);
                        if ( exist==null ) {
                            exist = new ImageInfo();
                            exist.setId(id);
                            SQLExecutor.insert(exist);
                        }

                        boolean image_exist = false;
                        if ( exist!=null && StringUtils.isNotBlank(exist.getFilepath()) ) {
                            File image = new File(exist.getFilepath());
                            if ( image.exists() && image.length() > 999 ) {
                                // 图片存在
                                image_exist  = true;
                            }
                        }


                        if ( !image_exist ) {
                            String imageFileName =  item.getSkuCode() + "_" + i1 + "_detail" + ".jpg";
                            File imageFile = new File(dir, imageFileName);
                            try {
                                HttpUtils.download(src, imageFile.getAbsolutePath());

                                exist.setRefid(item.getId());
                                exist.setCode(item.getSkuCode());
                                exist.setXh(i1);
                                exist.setType("D");
                                exist.setPicurl(src);
                                exist.setFilepath(imageFile.getAbsolutePath());

                                SQLExecutor.update(exist);

                                imageInfos.add(exist);
                            } catch (Exception e) {
                                System.out.println("图片下载失败: " + e.getMessage() + " ==> " + src);
                            }
                        }

                    } catch (Exception e) {

                    }
                    i1++;
                }
            }

        }
        return imageInfos;
    }

    private List<ImageInfo> loadImages(String shevlesid, String type) {

        List<ImageInfo> imageInfos = new ArrayList<>();
        try {
            imageInfos = SQLExecutor.executeQueryBeanList("select * from ITEM_IMAGE where REFID = ? and TYPE = ? ", ImageInfo.class, shevlesid, type);
        } catch (Exception e) {

        }
        return imageInfos;
    }


    public static void main(String[] args) {

        Matcher mat = DETAIL_IMAGE.matcher("background-image:url(//img30.360buyimg.com/sku/jfs/t1/62286/40/15739/348705/5dd1f8e3Eb9947898/c70c032bcd0f950e.jpg)}\n" +
                ".ssd-module-wrap .M15740418272072{height:1188px; background-size:100% 100%; width:750px; background-color:#f2f2f2; background-image:url(//img30.360buyimg.com/sku/jfs/t1/98345/29/2482/371722/5dd1f8e4E5d8c8db9/2940e481ad2b5aee.jpg)}\n" +
                ".ssd-module-wrap .M15740418272273{height:1188px; background-size:100% 100%; width:750px; background-color:#d7d7d7; ");

        while(mat.find()){
            String text = mat.group(1);
            System.out.println(text);
        }


    }
}
