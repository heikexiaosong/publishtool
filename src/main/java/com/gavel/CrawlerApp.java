package com.gavel;

import com.gavel.crawler.HtmlPageLoader;
import com.gavel.crawler.ItemSupplement;
import com.gavel.crawler.ProductPageLoader;
import com.gavel.crawler.SkuPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.*;

public class CrawlerApp {


    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final BlockingQueue<Task> taskQueue = new LinkedBlockingDeque<>();

    public static void main(String[] args) {

        new CrawlerApp();

    }


    public CrawlerApp() {

        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    loadSkus();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 3000, 5000, TimeUnit.MILLISECONDS);

    }



    private void loadSkus() throws Exception {

        System.out.println("Task: " + taskQueue.size());

        if ( taskQueue.isEmpty() ) {
            try {
                List<Task> tasks = SQLExecutor.executeQueryBeanList("select * from TASK   order by UPDATETIME desc", Task.class);
                System.out.println("未完成Task: " + taskQueue.size());
                if ( tasks!=null && tasks.size()>0 ) {
                    for (Task task : tasks) {
                        taskQueue.put(task);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if ( taskQueue.isEmpty() ) {

            try {
                List<BrandInfo> brandInfos = SQLExecutor.executeQueryBeanList("select * from BRAND_INFO where FLAG is null or FLAG <> 'X' order by SKUNUM desc", BrandInfo.class);
                System.out.println("brandInfos: " + brandInfos.size());
                if ( brandInfos!=null && brandInfos.size() > 0 ) {
                    for (BrandInfo brandInfo : brandInfos) {
                        System.out.println("品牌Task: " + brandInfo.getName1());
                        String url = brandInfo.getUrl();

                        try {
                            Task task = SQLExecutor.executeQueryBean("select * from TASK  where URL = ?", Task.class, url);
                            if ( task!=null ) {
                                System.out.println("Brand: " + brandInfo.getName1() + " exist!");
                                brandInfo.setFlag("X");
                                SQLExecutor.update(brandInfo);
                                continue;
                            }

                            System.out.println("Brand: " + brandInfo.getName1() + " run...");

                            task = new Task();

                            task.setUrl(url);
                            task.setTitle(brandInfo.getName1() + " " + ( brandInfo.getName2()==null ? "" : brandInfo.getName2() ));
                            task.setStatus("init");



                            try {
                                HtmlCache htmlCache = HtmlPageLoader.getInstance().loadHtmlPage(url, false);
                                if ( htmlCache!=null && htmlCache.getHtml()!=null ) {

                                    Document document = Jsoup.parse(htmlCache.getHtml());

                                    task.setTitle(document.title());

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
                                    task.setUpdatetime(Calendar.getInstance().getTime());


                                    try {
                                        SQLExecutor.insert(task);
                                        ItemSupplement.loadSearchItems(task);
                                        taskQueue.put(task);
                                        brandInfo.setFlag("X");
                                        SQLExecutor.update(brandInfo);
                                    } catch (Exception e) {
                                        System.out.println("[Task]" +brandInfo.getName1() + "任务生成失败");
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }



                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        Task task = taskQueue.poll();
        if ( task==null ) {
            return;
        }


        List<SearchItem> searchItemList = null;

        while (  task!=null ) {


            System.out.println("[" + task.getId() + "]爬取任务: " + task.getTitle());


            searchItemList =   SQLExecutor.executeQueryBeanList("select * from  SEARCHITEM where TASKID = ? and SKUNUM <> ACTUAL order by PAGENUM, XH ", SearchItem.class, task.getId());

            System.out.println("SearchItem: " + searchItemList.size());

            if ( searchItemList!=null && searchItemList.size() >0 ) {
                break;
            }

            task.setStatus("success");
            try {
                SQLExecutor.update(task);
            } catch (Exception e) {

            }

            task = taskQueue.poll();
        }

        if ( searchItemList==null || searchItemList.size()<=0 ) {
            return;
        }



        int total = searchItemList.size();
        for (int i = 0; i < total; i++) {
            boolean success = true;
            try {
                SearchItem searchItem = searchItemList.get(i);
                System.out.println("\r" + (i+1) + "/" + total + ". " + searchItem.getUrl() + ": " + searchItem.getSkunum());

                searchItem.setActual(searchItem.getSkunum());
                if ( searchItem.getType().equalsIgnoreCase("g") ){

                    List<Item>  skus = ProductPageLoader.getInstance().loadPage(searchItem);
                    searchItem.setStatus(SearchItem.Status.SUCCESS);
                    if ( skus==null || skus.size() < searchItem.getSkunum() ) {
                        searchItem.setActual((skus==null ? 0: skus.size()));
                        searchItem.setStatus(SearchItem.Status.EXCEPTION);
                        searchItem.setRemarks("预期: " + searchItem.getSkunum() + "; 实际: " + (skus==null ? 0: skus.size()));
                        System.out.println("\t...... 预期: " + searchItem.getSkunum() + "; 实际: " + (skus==null ? 0: skus.size()) );
                        success = false;
                    }

                } else {
                    Item item =  SkuPageLoader.getInstance().loadPage(searchItem);
                    searchItem.setStatus(SearchItem.Status.SUCCESS);
                    if ( item==null ) {
                        searchItem.setActual(0);
                        searchItem.setStatus(SearchItem.Status.EXCEPTION);
                        searchItem.setRemarks("");
                        System.out.println("\t...... load failed!");
                        success = false;
                    }
                }

                SQLExecutor.update(searchItem);
                if ( !success ) {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {

            }
        }


        task.setStatus("success");
        SQLExecutor.update(task);
        System.out.println("爬取任务[" + task.getTitle() + "]完成一轮.");
    }
}
