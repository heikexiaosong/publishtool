package com.gavel.crawler;

import com.gavel.config.APPConfig;
import com.gavel.entity.HtmlCache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class DriverHtmlLoader {

    private static final DriverHtmlLoader loader = new DriverHtmlLoader();

    public static DriverHtmlLoader getInstance() {
        return loader;
    }


    private final WebDriver driver;

    private final ChromeDriverService service;

    private final Pattern pattern =  Pattern.compile("(.*)(已加载完全部)(.*)");

    private DriverHtmlLoader() {


        String chromedriver = APPConfig.getInstance().getProperty("chromedriver", "");
        System.out.println("chromedriver: " + chromedriver );

        service = new ChromeDriverService.Builder().usingDriverExecutable(new File(chromedriver)).usingAnyFreePort().build();
        try {
            service.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("webdriver.chrome.driver", chromedriver);


        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized"); // https://stackoverflow.com/a/26283818/1689770
        options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
        //options.addArguments("--headless"); // only if you are ACTUALLY running headless
        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
//        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
//        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.managed_default_content_settings.images", 2);
        //chromePrefs.put("permissions.default.stylesheet", 2);
        chromePrefs.put("javascript", 2);
        options.setExperimentalOption("prefs", chromePrefs);

        driver = new ChromeDriver(options);

        //driver.get("https://www.grainger.cn/");
    }

    public HtmlCache loadHtmlPage(String url) {
        return loadHtmlPage(url, false);
    }


    public HtmlCache loadHtmlPage(String url, boolean loadmore) {

        HtmlCache cache = new HtmlCache();
        driver.navigate().to(url);
        String content = driver.getPageSource();
        if ( content==null || content.trim().length()==0 ) {
            return null;
        }

        if ( loadmore ) {
            try {
                driver.findElement(By.className("loadmore"));
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("LoadMoreProductSku()");
                Thread.sleep(5000);
                content = driver.getPageSource();
            } catch (Exception e) {

            }
        }

        cache = new HtmlCache();
        cache.setUrl(url.trim());
        cache.setHtml(content);
        cache.setContentlen(content.length());
        return cache;
    }

    public String loadHtml(String url) {
        return loadHtml(url, 3000);
    }

    public String loadHtml(String url, long millis) {
        return loadHtml(url, millis, false);
    }

    public String loadHtml(String url, long millis, boolean scrollTo) {
        System.out.println("URL: " + url.replace(" ", "%20"));
        driver.navigate().to(url.replace(" ", "%20"));

        try {
            if ( scrollTo ) {
                Thread.sleep(1000);
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,document.body.scrollHeight)");
            }
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String content = driver.getPageSource();
        return content;
    }

    public String loadHtml(String url, long millis, By by) {
        System.out.println("URL: " + url.replace(" ", "%20"));
        driver.navigate().to(url.replace(" ", "%20"));

        try {
            Thread.sleep(1000);
            WebDriverWait wait = new WebDriverWait(driver, 15);
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String content = driver.getPageSource();
        return content;
    }

    public void quit() {
        driver.close();
        driver.quit();
        service.stop();
    }

    public static void main(String[] args) {

        By by = By.xpath("span.p-price .price");

        getInstance().loadHtml("https://item.jd.com/100008751036.html", 1000, by);
    }

    public String loadPriceHtml(String url) {


        System.out.println("URL: " + url.replace(" ", "%20"));
        driver.navigate().to(url.replace(" ", "%20"));

        try {

            int cnt = 0;
            while ( cnt < 10 ) {
                Document doc = Jsoup.parse(driver.getPageSource());

                String priceStr = null;
                Element price = doc.selectFirst("span.p-price .price");
                if ( price!=null ) {
                    priceStr = price.text();
                    if ( priceStr!=null && priceStr.trim().length()>0 ) {
                        break;
                    }
                }

                Thread.sleep(1000);
                cnt++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String content = driver.getPageSource();
        return content;


    }
}
