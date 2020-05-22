package com.gavel.crawler;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import com.gavel.entity.SearchItem;
import com.gavel.entity.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final Pattern CODE_PATTERN = Pattern.compile("/([a-zA-Z])-([^\\.]*).html", Pattern.CASE_INSENSITIVE);

    private static final Pattern NUMBER = Pattern.compile("\\d*");

    public static void main(String[] args) throws Exception {

        //loadSearchItems("1589548611873");

        loadSkus("1589979520661");




    }


    public static void loadSkus(String taskid) throws Exception {

        if ( taskid==null || taskid.trim().length()==0 ) {
            System.out.println("[taskid: " + taskid + "]is blank!");
            return;
        }

        Task task = SQLExecutor.executeQueryBean("select * from task  where id = ? ", Task.class, taskid);

        if ( task==null ) {
            System.out.println("任务为空");
            return;
        }

        while ( true) {

            List<SearchItem> searchItemList =   SQLExecutor.executeQueryBeanList("select * from  SEARCHITEM where TASKID = ? and  SKUNUM <> ACTUAL order by skunum ", SearchItem.class, task.getId());

            System.out.println("Product: " + searchItemList.size());

            if ( searchItemList==null || searchItemList.size() ==0 ) {
                break;
            }

            for (int i = 0; i < searchItemList.size(); i++) {
                SearchItem searchItem = searchItemList.get(i);
                System.out.println( (i+1) + ". " + searchItem.getUrl() + ": " + searchItem.getSkunum());

                searchItem.setActual(searchItem.getSkunum());
                if ( searchItem.getType().equalsIgnoreCase("g") ){

                    List<Item>  skus = ProductPageLoader.getInstance().loadPage(searchItem);
                    searchItem.setStatus(SearchItem.Status.SUCCESS);
                    if ( skus==null || skus.size() < searchItem.getSkunum() ) {
                        searchItem.setActual((skus==null ? 0: skus.size()));
                        searchItem.setStatus(SearchItem.Status.EXCEPTION);
                        searchItem.setRemarks("预期: " + searchItem.getSkunum() + "; 实际: " + (skus==null ? 0: skus.size()));
                        System.out.println("\t...... 预期: " + searchItem.getSkunum() + "; 实际: " + (skus==null ? 0: skus.size()) );
                    }

                } else {
                    Item item =  SkuPageLoader.getInstance().loadPage(searchItem);
                    searchItem.setStatus(SearchItem.Status.SUCCESS);
                    if ( item==null ) {
                        searchItem.setActual(0);
                        searchItem.setStatus(SearchItem.Status.EXCEPTION);
                        searchItem.setRemarks("");
                        System.out.println("\t...... load failed!");
                    }
                }

                SQLExecutor.update(searchItem);
            }

            Thread.sleep(1000*60*10);


            System.out.println("Finish...");
        }


        DriverHtmlLoader.getInstance().quit();
    }



    public static void loadSearchItems(String taskid) throws Exception {

        if ( taskid==null || taskid.trim().length()==0 ) {
            System.out.println("[taskid: " + taskid + "]is blank!");
            return;
        }

        Task task = SQLExecutor.executeQueryBean("select * from task  where id = ? ", Task.class, taskid);

        if ( task==null ) {
            System.out.println("任务为空");
            return;
        }

        if (  task.getUrl()==null || task.getUrl().trim().length()==0  ) {
            System.out.println("[Task: " + task.getTitle() +"]任务URL为空");
            return;
        }

        int pageCur = 1;
        int pageTotal = task.getPagenum();

        while ( pageCur <= pageTotal ) {
            String pageUrl = "https://www.grainger.cn/s-1.html?page=" + pageCur;
            HtmlCache htmlCache = HtmlPageLoader.getInstance().loadHtmlPage(pageUrl, false);

            if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
                System.out.println("[URL: " + pageUrl + "]网页打开失败");
                continue;
            }


            Document document = Jsoup.parse(htmlCache.getHtml());

            Elements proUL = document.select("div.proUL li");
            for (int i = 0; i < proUL.size(); i++) {
                Element element = proUL.get(i);

                SearchItem searchItem = new SearchItem();
                searchItem.setId(task.getId() + "-" + pageCur + "-" + (i+1));
                searchItem.setTaskid(task.getId());
                searchItem.setPagenum(pageCur);
                searchItem.setXh(i+1);


                searchItem.setTitle(element.attr("title"));

                Element item = element.selectFirst("a");

                // 图片
                Element img = item.selectFirst("div.pic > img");
                String picUrl = img.attr("src");
                if ( "/Content/images/hp_np.png".equalsIgnoreCase(picUrl) ) {
                    searchItem.setPic("https://www.grainger.cn/Content/images/hp_np.png");
                } else {
                    searchItem.setPic("https:" + picUrl);
                }


                int cnt = 1;
                Element em = item.selectFirst("div.wenz > div > em");
                Matcher matcher = NUMBER.matcher(em.text());
                if (matcher.find()) {
                    cnt = Integer.parseInt(matcher.group(0));
                    searchItem.setSkunum(cnt);
                }

                String href = item.attr("href");
                searchItem.setUrl("https://www.grainger.cn" + href);

                matcher = CODE_PATTERN.matcher(href);
                if (matcher.find()) {
                    searchItem.setType(matcher.group(1));
                    searchItem.setCode(matcher.group(2));
                }

                SQLExecutor.insert(searchItem);
            }

            System.out.println("Page: " + pageCur + " => " + proUL.size());
            pageCur++;

        }

    }

    private static void task() throws Exception {
        String url = "https://www.grainger.cn/s-1.html";

        HtmlCache htmlCache = HtmlPageLoader.getInstance().loadHtmlPage(url, false);

        if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
            System.out.println("[URL: " + url + "]网页打开失败");
            return;
        }

        Task task = new Task();
        task.setTitle("");

        Document document = Jsoup.parse(htmlCache.getHtml());
        Element cpz = document.selectFirst("font.cpz");
        Element total = document.selectFirst("font.total");
        System.out.println("产品组: " + cpz.text() + "; 产品: " + total.text());

        task.setProductnum(Integer.parseInt(cpz.text()));
        task.setSkunum(Integer.parseInt(total.text()));

        int pageCur = 0;
        int pageTotal = 0;
        Elements labels = document.select("div.pagination > label");
        if ( labels.size()==2 ) {
            pageCur = Integer.parseInt(labels.get(0).text());
            pageTotal = Integer.parseInt(labels.get(1).text());
        }

        System.out.println("当前页: " + pageCur);
        System.out.println("总页数: " + pageTotal);

        task.setPagenum(pageTotal);


        task.setTitle(document.title());
        task.setUrl(url);
        task.setStatus(Task.Status.INIT);
        task.setUpdatetime(Calendar.getInstance().getTime());


        SQLExecutor.insert(task);
    }


}
