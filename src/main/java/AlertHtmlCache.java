import com.gavel.database.SQLExecutor;
import com.gavel.entity.PHtmlCache;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AlertHtmlCache {

    private static ExecutorService executor = Executors.newFixedThreadPool(50);

    public static void main(String[] args) throws Exception {


        final int PAGE_SIZE = 500;


        int total = 0;

       List<PHtmlCache> items = SQLExecutor.executeQueryBeanList("select * from HTMLCACHE limit ? ", PHtmlCache.class, PAGE_SIZE);

       while ( items!=null && items.size()>0 ) {
           System.out.print(items.size());

           total += items.size();


           final List<Future> futures = new ArrayList<>();

           for (final PHtmlCache item : items) {

               futures.clear();
                   Future<?>  future = executor.submit(new Runnable() {
                       @Override
                       public void run() {
                               try {
                                   item.setId(MD5Utils.md5Hex(item.getUrl()));

                                   String suffix =  item.getId().substring(item.getId().length()-1);

                                   PHtmlCache exist = SQLExecutor.executeQueryBean("select * from HTMLCACHE_" + StringUtils.trim(suffix) + " where ID = ? ", PHtmlCache.class, item.getId());
                                   if ( exist==null ) {
                                       Document doc = Jsoup.parse(item.getHtml());
                                       doc.select("script").remove();
                                       item.setHtml(doc.outerHtml());

                                       item.setContentlen(item.getHtml().length());
                                       SQLExecutor.insert(item, item.getId().substring(item.getId().length()-1));
                                   } else {
                                       if ( item.getUrl().equalsIgnoreCase(exist.getUrl()) ) {

                                       } else {
                                           System.out.println("MD5 Conflict" + exist.getUrl() + " ==> " + item.getUrl());
                                       }
                                   }

                                   SQLExecutor.execute("DELETE from HTMLCACHE where  URL = ?", item.getUrl());
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                       }
                   });

                   futures.add(future);

           }

           for (Future future : futures) {
               future.get();
           }



           System.out.println(" ==> total: " + total);

           items = SQLExecutor.executeQueryBeanList("select * from HTMLCACHE limit ? ", PHtmlCache.class, PAGE_SIZE);
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
