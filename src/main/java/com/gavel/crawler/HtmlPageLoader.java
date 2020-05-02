package com.gavel.crawler;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public class HtmlPageLoader {

    private static final  HtmlPageLoader loader = new HtmlPageLoader();

    public static HtmlPageLoader getInstance() {
        return loader;
    }


    private final WebDriver driver;

    private final Pattern pattern =  Pattern.compile("(.*)(已加载完全部)(.*)");

    private HtmlPageLoader() {

        System.setProperty("webdriver.chrome.driver","C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");


        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized"); // https://stackoverflow.com/a/26283818/1689770
        options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
//        options.addArguments("--headless"); // only if you are ACTUALLY running headless
//        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
//        options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
//        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
//        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
//        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.managed_default_content_settings.images", 2);
        chromePrefs.put("permissions.default.stylesheet", 2);
        chromePrefs.put("javascript", 2);
        options.setExperimentalOption("prefs", chromePrefs);

        driver = new ChromeDriver(options);

        driver.get("https://www.grainger.cn/");

    }

    public HtmlCache loadHtmlPage(String url, String params) throws Exception {

        HtmlCache cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, url);
        if ( cache == null ) {
            StringBuilder urlBuilder = new StringBuilder(url.trim());
            if ( params!=null && params.trim().length() > 0 ){
                urlBuilder.append(params.trim());
            }


            driver.get(urlBuilder.toString().trim());
            String content = driver.getPageSource();
            if ( content==null || content.trim().length()==0 ) {
                return null;
            }

            cache = new HtmlCache();
            cache.setUrl(url.trim());
            cache.setHtml(content);
            cache.setContentlen(content.length());
            cache.setUpdatetime(Calendar.getInstance().getTime());
            SQLExecutor.insert(cache);
        }
        return cache;
    }

    public HtmlCache loadHtmlPage(String url, boolean useCache) throws Exception {

        return loadHtmlPage(url, useCache, false);
    }

    public HtmlCache loadHtmlPage(String url, boolean useCache, boolean loadMore) throws Exception {

        HtmlCache cache = null;
        if ( useCache ) {
            cache =  SQLExecutor.executeQueryBean("select * from htmlcache  where url = ? limit 1 ", HtmlCache.class, url);
        }
        if ( cache == null ) {
            driver.navigate().to(url);
            String content = driver.getPageSource();
            if ( content==null || content.trim().length()==0 ) {
                return null;
            }



            Document document = Jsoup.parse(content);

            if ( loadMore && document.selectFirst("a.loadmore")!=null ) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("LoadMoreProductSku()");
                Thread.sleep(5000);
//                WebDriverWait wait=new WebDriverWait(driver,10);
//                wait.until(ExpectedConditions.textMatches(By.className("loadmore"), pattern));
                content = driver.getPageSource();
            }

            cache = new HtmlCache();
            cache.setUrl(url.trim());
            cache.setHtml(content);
            cache.setContentlen(content.length());
        }
        return cache;
    }

    public void quit() {
        driver.close();
        driver.quit();
    }
}
