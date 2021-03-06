package com.gavel.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.regex.Pattern;

public class NewDriverHtmlLoader {

    private static final NewDriverHtmlLoader loader = new NewDriverHtmlLoader();

    public static NewDriverHtmlLoader getInstance() {
        return loader;
    }


    private final WebDriver driver;

    private final Pattern pattern =  Pattern.compile("(.*)(已加载完全部)(.*)");

    private NewDriverHtmlLoader() {

        System.setProperty("webdriver.chrome.driver","C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");


        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized"); // https://stackoverflow.com/a/26283818/1689770
        options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
        options.addArguments("--headless"); // only if you are ACTUALLY running headless
        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
//        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
//        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.managed_default_content_settings.images", 2);
        chromePrefs.put("permissions.default.stylesheet", 2);
        options.setExperimentalOption("prefs", chromePrefs);

        driver = new ChromeDriver(options);

        driver.get("https://www.grainger.cn/");
    }

    public String loadHtmlPage(String url) {
        return loadHtmlPage(url, false);
    }


    public String loadHtmlPage(String url, boolean loadmore) {

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

        return content;
    }

    public void quit() {
        driver.close();
        driver.quit();
    }

    public static void main(String[] args) {
        getInstance().loadHtmlPage("https://www.grainger.cn/g-384478.html", true);
    }
}
