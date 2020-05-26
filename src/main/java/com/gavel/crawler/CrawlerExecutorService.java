package com.gavel.crawler;

import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Item;
import com.gavel.entity.SearchItem;
import com.gavel.entity.Task;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrawlerExecutorService {

    private final ScheduledExecutorService executors;

    private static final CrawlerExecutorService INSTANCE = new CrawlerExecutorService();


    private BlockingQueue<Task> taskQueue = new LinkedBlockingDeque<>();

    private AtomicBoolean running = new AtomicBoolean(true);

    public CrawlerExecutorService() {
        String booleanStr = APPConfig.getInstance().getProperty("crawler.running", "false");
        running.set(Boolean.parseBoolean(booleanStr));

        System.out.println("Crawler Runing: " + running.get());
        this.executors = Executors.newSingleThreadScheduledExecutor();
    }

    public static CrawlerExecutorService getInstance() {
        return INSTANCE;
    }


    public void addTask(Task task) {
        if ( task!=null ) {
            taskQueue.add(task);
        }
    }

    public boolean getRunning() {
        return running.get();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public void start() {

        executors.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if ( running.get() ) {
                    try {
                        loadSkus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);


        try {
            List<Task> tasks = SQLExecutor.executeQueryBeanList("select * from TASK where STATUS <> 'success' order by UPDATETIME desc", Task.class);
            if ( tasks!=null && tasks.size()>0 ) {
                for (Task task : tasks) {
                    taskQueue.put(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadSkus() throws Exception {


        if ( taskQueue.isEmpty() ) {
            try {
                List<Task> tasks = SQLExecutor.executeQueryBeanList("select * from TASK  where STATUS <> 'success'  order by UPDATETIME desc", Task.class);
                if ( tasks!=null && tasks.size()>0 ) {
                    for (Task task : tasks) {
                        taskQueue.put(task);
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

        System.out.println("爬取任务: " + task.getTitle());


        List<SearchItem> searchItemList =   SQLExecutor.executeQueryBeanList("select * from  SEARCHITEM where TASKID = ? and SKUNUM <> ACTUAL order by PAGENUM, XH ", SearchItem.class, task.getId());

        System.out.println("SearchItem: " + searchItemList.size());

        if ( searchItemList==null || searchItemList.size() ==0 ) {
            task.setStatus("success");
            try {
                SQLExecutor.update(task);
            } catch (Exception e) {

            }
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

        System.out.println("爬取任务[" + task.getTitle() + "]完成一轮.");
    }
}
