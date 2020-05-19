package com.gavel.crawler;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
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

        HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, url);
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

    public HtmlCache loadHtmlPage(String url, boolean useCache) throws Exception {

        return loadHtmlPage(url, useCache, false);
    }

    public HtmlCache loadHtmlPage(String url, boolean useCache, boolean loadMore) {

        System.out.println("URL: " + url);
        HtmlCache cache = null;
        if ( useCache ) {
            try {
                cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, url);
            } catch (Exception e) {
                System.out.println("[executeQueryBean]SQLExecutor: " + e.getMessage());
            }
        }

        if ( cache!=null && cache.getHtml()!=null )  {
            Document doc = Jsoup.parse(cache.getHtml());
            if ( doc.title().equalsIgnoreCase("403 Forbidden") ) {
                try {
                    SQLExecutor.delete(cache);
                } catch (Exception e) {
                    System.out.println("[delete]SQLExecutor: " + e.getMessage());
                }
                cache = null;
            }
        }

        if ( cache == null ) {
            cache = DriverHtmlLoader.getInstance().loadHtmlPage(url, loadMore);
            if ( cache!=null  ) {
                cache.setUpdatetime(Calendar.getInstance().getTime());
                try {
                    SQLExecutor.insert(cache);
                } catch (Exception e) {
                    System.out.println("[insert]SQLExecutor: " + e.getMessage());
                }
            }
        }
        return cache;
    }
}
