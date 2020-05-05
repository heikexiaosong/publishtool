import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.ImageCache;
import com.gavel.entity.Item;
import com.gavel.utils.ImageCachedLoader;
import com.google.common.io.Files;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class PicDownload {

    public static void main(String[] args) throws Exception {


       List<Item> items = SQLExecutor.executeQueryBeanList("select * from ITEM ", Item.class);

        System.out.println(items.size());


        BufferedWriter writer = Files.newWriter(new File("picinfo.csv"), Charset.forName("GB2312"));


        for (int i = 0; i < items.size(); i++) {

            try {
                Item item = items.get(i);
                System.out.print( "\r" + i + ". " + item.getCode());


                HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, item.getUrl());
                if ( cache==null ) {
                    writer.write(item.getCode() + "," + item.getName() + "," + item.getUrl() + ", htmlCache is null" );
                    writer.newLine();
                    System.out.println(" htmlCache is null \n");
                    continue;
                }


                Document doc = Jsoup.parse(cache.getHtml());

                Element err = doc.selectFirst("div.err-notice");
                if ( err!=null ) {
                    throw new Exception("[" + item.getUrl() + "]页面未找到");
                }

                // 品牌 + 标题
                Element proDetailCon = doc.selectFirst("div.proDetailCon");
                if ( proDetailCon==null ) {
                    throw new Exception("[" + item.getUrl() + "]Html内容有异常: " + doc.title());
                }

                writer.write(item.getCode() + "," + item.getName() + "," + item.getUrl() + ", " );


                Elements imgs = doc.select("div.xiaotu > div.xtu > dl > dd > img");
                writer.write((imgs==null?0:imgs.size()) + ", " );
                item.setPicnum(imgs.size());
                SQLExecutor.update(item);

                for (Element img : imgs) {
                    String  src = img.attr("src");

                    if ( src.equalsIgnoreCase("/Content/images/hp_np.png") ) {
                        src = "https://www.grainger.cn/Content/images/hp_np.png";
                    }

                    if ( src!=null && src.startsWith("//") ) {
                        src = "https:" + src;
                    }

                    src = src.replace("product_images_new/350/", "product_images_new/800/");

                    ImageCache imgCache = ImageCachedLoader.loadIamge(src);

                    if ( imgCache==null || imgCache.getFilepath()==null || imgCache.getFilepath().trim().length()==0 ) {
                        System.out.println("[Img: " + src + "]download failed.");
                        continue;
                    }

                    File image = new File(ImageCachedLoader.PICS_DIR, imgCache.getFilepath());
                    if ( !image.exists() ) {
                        System.out.println("[Img: " + src + "][File: " + image + "]not exist");
                        continue;
                    }


                    File new_image = new File("E:\\物业", imgCache.getFilepath());
                    if ( !new_image.getParentFile().exists() ) {
                        new_image.getParentFile().mkdirs();
                    }

                    Files.copy(image, new_image);

                    writer.write(src + ", " );
                }
            } catch (Exception e) {
                System.out.println(": " + e.getMessage());
            }

            writer.newLine();

            writer.flush();

        }
        writer.close();

    }

}
