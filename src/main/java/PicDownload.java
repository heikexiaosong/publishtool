import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class PicDownload {

    public static void main(String[] args) throws Exception {


       List<Item> items = SQLExecutor.executeQueryBeanList("select * from ITEM", Item.class);

        System.out.println(items.size());


        for (int i = 0; i < items.size(); i++) {

            try {
                Item item = items.get(i);
                System.out.print( "\r[" + Math.ceil(i*100/items.size()) + "%]" + i + ". " + item.getCode() + " " + item.getUrl());


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

                item.setName(c6.text());
                item.setCategoryname(c4.text());

                SQLExecutor.update(item);




            } catch (Exception e) {

                e.printStackTrace();
                System.out.println(": " + e.getMessage());
            }


        }

    }

}
