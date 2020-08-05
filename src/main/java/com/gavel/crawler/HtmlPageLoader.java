package com.gavel.crawler;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.PHtmlCache;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Calendar;

public class HtmlPageLoader {

    private static final  HtmlPageLoader loader = new HtmlPageLoader();

    public static HtmlPageLoader getInstance() {
        return loader;
    }

    private HtmlPageLoader() {

    }

    public HtmlCache loadHtmlPage(String url, String params) throws Exception {

        String id = MD5Utils.md5Hex(url);
        String suffix =  id.substring(id.length()-1);

        HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache_"+ StringUtils.trim(suffix) + "  where ID = ? limit 1 ", HtmlCache.class, id);
        if ( cache == null ) {
            cache = DriverHtmlLoader.getInstance().loadHtmlPage(url);
            if ( cache!=null && cache.getUpdatetime()==null ) {
                cache.setUpdatetime(Calendar.getInstance().getTime());
                SQLExecutor.insert(cache);
            }
        }
        return cache;
    }

    public HtmlCache loadGraingerPage(String skuCode, boolean useCache) throws Exception {

        if (StringUtils.isBlank(skuCode)) {
            return null;
        }

        String url = "https://www.grainger.cn/u-" + skuCode.trim() + ".html";
        return loadHtmlPage(url, useCache, false);
    }

    public HtmlCache loadJDPage(String skuCode, boolean useCache) throws Exception {

        if (StringUtils.isBlank(skuCode)) {
            return null;
        }

        String url = "https://item.jd.com/" + skuCode.trim() + ".html";
        return loadHtmlPage(url, useCache, false);
    }

    public HtmlCache loadHtmlPage(String url, boolean useCache) throws Exception {

        return loadHtmlPage(url, useCache, false);
    }

    public HtmlCache loadHtmlPage(String url, boolean useCache, boolean loadMore) {
        HtmlCache cache = null;
        if ( useCache ) {
            try {
                String id = MD5Utils.md5Hex(url);
                String suffix =  id.substring(id.length()-1);
                System.out.println(url + " => " + id + " : " + suffix);
                cache =  SQLExecutor.executeQueryBean("select * from htmlcache_"+ StringUtils.trim(suffix) + "  where ID = ? limit 1 ", PHtmlCache.class, id);
            } catch (Exception e) {
                System.out.println("[executeQueryBean]SQLExecutor: " + url);
            }
        }

        if ( cache!=null && cache.getHtml()!=null )  {
            Document doc = Jsoup.parse(cache.getHtml());
            if ( doc.title().equalsIgnoreCase("403 Forbidden") ) {
                try {
                    SQLExecutor.delete(cache);
                    cache = null;
                } catch (Exception e) {
                    System.out.println("[delete]SQLExecutor: " + url);
                }
            }  else if ( doc.title().equalsIgnoreCase("Error") ) {
                try {
                    SQLExecutor.delete(cache);
                    cache = null;
                } catch (Exception e) {
                    System.out.println("[delete]SQLExecutor: " + url);
                }
            }
        }

        if ( cache == null ) {
            cache = loadSkuHtmlPage(url, loadMore, 0);
        }
        return cache;
    }


    public HtmlCache loadSkuHtmlPage(String url, boolean useCache, int times) {
        HtmlCache cache = null;
        if ( useCache ) {
            try {
                String id = MD5Utils.md5Hex(url);
                String suffix =  id.substring(id.length()-1);
                cache =  SQLExecutor.executeQueryBean("select * from htmlcache_"+ StringUtils.trim(suffix) + "  where ID = ? limit 1 ", PHtmlCache.class, id);
            } catch (Exception e) {
                System.out.println("[executeQueryBean]SQLExecutor: " + e.getMessage());
            }
        }

        if ( cache!=null && cache.getHtml()!=null )  {
            Document doc = Jsoup.parse(cache.getHtml());
            if ( doc.title().equalsIgnoreCase("403 Forbidden") ) {
                try {
                    SQLExecutor.delete(cache);
                    cache = null;
                } catch (Exception e) {
                    System.out.println("[delete]SQLExecutor: " + e.getMessage());
                }
            }  else if ( doc.title().equalsIgnoreCase("Error") ) {
                try {
                    SQLExecutor.delete(cache);
                    cache = null;
                } catch (Exception e) {
                    System.out.println("[delete]SQLExecutor: " + e.getMessage());
                }
            }
        }

        if ( cache == null ) {


//            OkHttpClient client = HttpProxyClient.getInstance().defaultClient();
//            if ( times > 0 ) {
//                client = HttpProxyClient.getIDrnstance().getClient();
//            }

            try {
                //String html =  HttpUtils.get(url, client);

                String html = DriverHtmlLoader.getInstance().loadHtml(url);
                cache = new PHtmlCache();
                ((PHtmlCache) cache).setId(MD5Utils.md5Hex(url));
                cache.setUrl(url.trim());
                cache.setHtml(html);
                cache.setContentlen(html.length());
                //HttpProxyClient.getInstance().release(client);
            } catch (Exception e) {
                System.out.println("[HttpUtils]: " + e.getMessage());
            }
        }
        return cache;
    }

    public static void main(String[] args) throws Exception {
        getInstance().loadHtmlPage("https://item.jd.com/65280974820.html", true);
    }
}
