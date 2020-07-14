package com.gavel;

import com.gavel.crawler.DriverHtmlLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.SearchItem;
import com.gavel.entity.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JDScene {

    public static void main(String[] args) throws Exception {

        Task task = buildTask("https://pro.jd.com/mall/active/2bVj4P4o6ycaEB3od8CfsAdCRKSP/index.html", "雨季防汛好货精选");

        SQLExecutor.insert(task);

        String html = DriverHtmlLoader.getInstance().loadHtml(task.getUrl());
        Document document = Jsoup.parse(html);

        System.out.println(document.title());

        Elements modules = document.select("div.pd_module");
        System.out.println(modules.size());

        int pageCur = 0;
        for (Element module : modules) {
            pageCur++;

            int xh = 0;
            Elements items = module.select("div.pd_common_wrap > a");
            for (Element item : items) {
                xh++;
                System.out.println(item.outerHtml());
                System.out.println("---------------------------");

                SearchItem searchItem = new SearchItem();
                searchItem.setId(task.getId() + "-" + pageCur + "-" + xh);
                searchItem.setTaskid(task.getId());
                searchItem.setPagenum(pageCur);
                searchItem.setXh(xh++);
                searchItem.setCode(item.attr("data-sku"));
                searchItem.setUrl("https:" + item.attr("href"));
                searchItem.setTitle(item.selectFirst("div.pd_title").text());
                searchItem.setSkunum(1);
                searchItem.setActual(0);
                searchItem.setType("u");

                SQLExecutor.insert(searchItem);
            }
        }


    }


    private static Task buildTask(String url, String title) throws Exception {

        Task task = SQLExecutor.executeQueryBean("select * from TASK  where TYPE = 'JD' and URL = ? ", Task.class, url);
        if ( task!=null ) {
            return task;
        }


        task = new Task();

        task.setUrl(url);
        task.setTitle(title);

        task.setType("JD");
        task.setStatus("init");


        return task;
    }
}
