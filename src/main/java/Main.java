import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import com.gavel.grainger.StringUtils;
import com.google.common.io.Files;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {


       List<Item> items = SQLExecutor.executeQueryBeanList("select * from ITEM", Item.class);

        System.out.println(items.size());


        BufferedWriter writer = Files.newWriter(new File("brand.csv"), Charset.forName("GB2312"));

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            System.out.println( "\r" + i + ". " + item.getCode());


            HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, item.getUrl());
            if ( cache==null ) {
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


            // 4级类目 + 产品组ID + ID
            Elements elements = doc.select("div.crumbs  a");
            Element c1 = elements.get(1);
            Element c2 = elements.get(2);
            Element c3 = elements.get(3);
            Element c4 = elements.get(4);
            Element c5 = elements.get(5);
            Element c6 = elements.get(6);


            writer.write(StringUtils.getCode(c1.attr("href")) + ",");
            writer.write(c1.text() + ",");
            writer.write(StringUtils.getCode(c2.attr("href")) + ",");
            writer.write(c2.text() + ",");
            writer.write(StringUtils.getCode(c3.attr("href")) + ",");
            writer.write(c3.text() + ",");
            writer.write(StringUtils.getCode(c4.attr("href")) + ",");
            writer.write(c4.text() + ",");
            writer.write(StringUtils.getCode(c5.attr("href")) + ",");
            writer.write(c5.text() + ",");
            writer.write(StringUtils.getCode(c6.attr("href")) + ",");
            writer.write(c6.text() + ",");


            // 标题前 品牌
            String brand1 =  proDetailCon.selectFirst("h3 > span > a").html();


            Elements fonts = proDetailCon.select("div font");
            String brand = fonts.get(1).text();
            String model = fonts.get(2).text();

            /**
             * 订 货 号：5W8061
             * 品   牌：霍尼韦尔 Honeywell
             * 制造商型号： SHSL00202-42
             * 包装内件数：1双
             * 预计发货日： 停止销售
             *

             String code = fonts.get(0).text();
             String brand = fonts.get(1).text();
             String model = fonts.get(2).text();
             String number = fonts.get(3).text();
             String fahuori = fonts.get(4).text();
             */

            if ( brand1.trim().equalsIgnoreCase(brand.trim()) ) {
                writer.write(brand1.trim() + ",");

                writer.write(brand.trim() + ",");
            } else {
                writer.write(brand1.trim() + ",");

                writer.write(brand.replace(brand1, "").trim() + ",");
            }





            writer.write(model + ",");

            writer.write(item.getCode() + ",");

            Elements prices = doc.select("div.price b");
            if ( prices.size()==1 ) {
                writer.write(prices.get(0).text().replace(",", "").replace("¥", "").trim() + ", ,");

            } else  if ( prices.size()==2 )  {
                writer.write(prices.get(0).text().replace(",", "").replace("¥", "").trim() + ", " + prices.get(1).text().replace(",", "").replace("¥", "").trim() + ",");
            }

            writer.write(item.getUrl() + "\n");

        }

    }
}
