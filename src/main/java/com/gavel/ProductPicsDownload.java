package com.gavel;

import com.gavel.crawler.HtmlPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import com.gavel.entity.SearchItem;
import com.gavel.utils.ImageLoader;
import com.google.common.io.Files;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ProductPicsDownload {



    public static void main(String[] args) throws Exception {

    }

    public static void download(SearchItem searchItem, String _dir) throws Exception {

        if (searchItem == null) {
            return;
        }


        File dir = new File(_dir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if ("g".equalsIgnoreCase(searchItem.getType())) {

            List<Item> items = Collections.EMPTY_LIST;
            try {
                items = SQLExecutor.executeQueryBeanList("select * from ITEM where PRODUCTCODE = ? ", Item.class, searchItem.getCode());
            } catch (Exception e) {

            }

            for (Item item : items) {
                downloadSkuPics(item.getCode(), dir);
            }


        } else if ("u".equalsIgnoreCase(searchItem.getType())) {
            downloadSkuPics(searchItem.getCode(), dir);
        }


    }

        public static void downloadSkuPics(String skucode, File _dir) throws Exception {

            long start = System.currentTimeMillis();

            HtmlCache cache = HtmlPageLoader.getInstance().loadGraingerPage(skucode, true);
            if (cache == null) {
                throw new Exception("[Sku: " + skucode + "]htmlCache is null \n");
            }


            Document doc = Jsoup.parse(cache.getHtml());
            if ( doc!=null && doc.title().equalsIgnoreCase("403 Forbidden") ) {
                throw new Exception("[" + cache.getUrl() + "]403 Forbidden");
            }
            if ( doc!=null && doc.title().equalsIgnoreCase("Error") ) {
                throw new Exception("[" + cache.getUrl() + "]Error");
            }

            Element err = doc.selectFirst("div.err-notice");
            if (err != null) {
                throw new Exception("[" + cache.getUrl() + "]页面未找到");
            }

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

                String _image = src.replace("https://static.grainger.cn/", "").replace("/", File.separator).trim();
                if ( src.equalsIgnoreCase("https://www.grainger.cn/Content/images/hp_np.png") ) {
                    _image = src.replace("https://www.grainger.cn/", "").replace("/", File.separator).trim();
                }

                File existFile = new File(ImageLoader.PICS_COMPLETE_DIR + File.separator + _image);
                if ( existFile.exists() ) {
                    System.out.println("[" +  existFile + "]文存在.");
                    continue;
                }

                File destFile = new File(_dir.getAbsolutePath() + File.separator + _image);
                if ( !destFile.getParentFile().exists() ) {
                    destFile.getParentFile().mkdirs();
                }

                existFile = new File(ImageLoader.PICS_DIR + File.separator + _image);
                if ( existFile.exists() ) {
                    Files.copy(existFile, destFile);
                    continue;
                }

                HttpUtils.download(src, destFile.getAbsolutePath());
            }

            System.out.print("\r[SKU: " + skucode + "]Download Image. Cost: " + (System.currentTimeMillis() - start) + " ms");
        }

}
