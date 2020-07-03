import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.ShelvesItem;
import com.gavel.utils.MD5Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class PicDownload {

    public static void main(String[] args) throws Exception {


       List<ShelvesItem> items = SQLExecutor.executeQueryBeanList("select * from  SHELVESITEM where TASKID = ? ", ShelvesItem.class, "1588575807900");

        System.out.println(items.size());


        for (int i = 0; i < items.size(); i++) {

            try {
                ShelvesItem item = items.get(i);
                System.out.print( "\r[" + Math.ceil(i*100/items.size()) + "%]" + i + ". " + item.getItemCode() + " " + item.getCmTitle());



                String id = MD5Utils.md5Hex("https://www.grainger.cn/u-" + item.getItemCode() + ".html");
                String suffix =  id.substring(id.length()-1);
                HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache_"+ com.gavel.utils.StringUtils.trim(suffix) + "  where ID = ? limit 1 ", HtmlCache.class, id);

               // HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, "https://www.grainger.cn/u-" + item.getItemCode() + ".html");
                if ( cache==null ) {
                    System.out.println(" htmlCache is null \n");
                    continue;
                }


                Document doc = Jsoup.parse(cache.getHtml());

                Element err = doc.selectFirst("div.err-notice");
                if ( err!=null ) {
                    throw new Exception("[" + item.getItemCode() + "]页面未找到");
                }

            } catch (Exception e) {

                e.printStackTrace();
                System.out.println(": " + e.getMessage());
            }


        }

    }

}
