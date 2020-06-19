import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.PHtmlCache;
import com.gavel.utils.MD5Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

public class AlertHtmlCache {

    public static void main(String[] args) throws Exception {


        final int PAGE_SIZE = 500;


        int total = 0;

       List<HtmlCache> items = SQLExecutor.executeQueryBeanList("select URL from HTMLCACHE limit ? ", HtmlCache.class, PAGE_SIZE);

       while ( items!=null && items.size()>0 ) {
           System.out.println(items.size());

           total += items.size();


           for (HtmlCache item : items) {
               PHtmlCache pHtmlCache = SQLExecutor.executeQueryBean("select * from HTMLCACHE where URL = ? ", PHtmlCache.class, item.getUrl());

               pHtmlCache.setId(MD5Utils.md5Hex(item.getUrl()));

               Document doc = Jsoup.parse(pHtmlCache.getHtml());
               doc.select("script").remove();
               pHtmlCache.setHtml(doc.outerHtml());


               pHtmlCache.setContentlen(pHtmlCache.getHtml().length());
               SQLExecutor.insert(pHtmlCache, pHtmlCache.getId().substring(pHtmlCache.getId().length()-1));

               SQLExecutor.delete(item);

           }

           System.out.println("total: " + total);

           items = SQLExecutor.executeQueryBeanList("select URL from HTMLCACHE limit ? ", HtmlCache.class, PAGE_SIZE);
       }






    }

    private static String escape(String text) {
        String res = text;
        if ( text!=null && text.contains(",") ) {
            res = "\"" + text + "\"";
        }

        return res;
    }
}
