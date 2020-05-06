import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class DetailPicDownload {

    public static void main(String[] args) throws Exception {


       List<Item> items = SQLExecutor.executeQueryBeanList("select * from ITEM ", Item.class);

        System.out.println(items.size());

        for (int i = 0; i < items.size(); i++) {

            try {
                Item item = items.get(i);
                System.out.print( "\r" + i + ". " + item.getCode());


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
                Elements proDetailCon = doc.selectFirst("div.proDetailDiv > div").select("div.proDetailTit");
                if ( proDetailCon==null ) {
                    throw new Exception("[" + item.getUrl() + "]Html内容有异常: " + doc.title());
                }


                if ( proDetailCon.size() > 2 ) {
                    System.out.println("");
                    for (Element element : proDetailCon) {
                        System.out.println("\t" + element.text() );
                    }
                    System.out.println("");
                }



            } catch (Exception e) {
                System.out.println(": " + e.getMessage());
            }

        }

    }

}
