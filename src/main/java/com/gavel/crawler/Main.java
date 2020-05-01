package com.gavel.crawler;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {

        List<HtmlCache> exists =  SQLExecutor.executeQueryBeanList("select URL from htmlcache", HtmlCache.class);

        Set<String> urlSet = new HashSet<>();


        for (HtmlCache exist : exists) {
            urlSet.add(exist.getUrl());
        }

        for (HtmlCache exist : exists) {
            String url = exist.getUrl();
            if ( url.startsWith("https://www.grainger.cn//") ) {
                url = url.replace("https://www.grainger.cn//", "https://www.grainger.cn/");
                if ( urlSet.contains(url) ) {
                    SQLExecutor.execute("delete from htmlcache where url = ?",  exist.getUrl());
                }
            }
        }


//        System.setProperty("webdriver.chrome.driver","C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
//
//
//        WebDriver driver = new ChromeDriver();
//        driver.get("http://www.baidu.com");
//
//        String title = driver.getTitle();
//        System.out.printf(title);
//
//        Thread.sleep(10000);
//
//        driver.close();
//
//        driver.quit();

    }
}
