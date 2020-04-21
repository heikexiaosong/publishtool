package com.gavel.crawler;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        System.setProperty("webdriver.chrome.driver","C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");


        WebDriver driver = new ChromeDriver();
        driver.get("http://www.baidu.com");

        String title = driver.getTitle();
        System.out.printf(title);

        Thread.sleep(10000);

        driver.close();

        driver.quit();

    }
}
