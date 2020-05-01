package com.gavel.crawler;

import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.utils.MD5Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProxyCrawler {


    private final ExecutorService executors = Executors.newFixedThreadPool(20);

    private static final ProxyCrawler instance = new ProxyCrawler();

    public static ProxyCrawler getInstance() {
        return instance;
    }

    private final OkHttpClient.Builder builder = new OkHttpClient.Builder();

    private ProxyCrawler() {
    }



    public void start() {

      String ip3366 =  "http://www.ip3366.net/free/?stype=1&page=";

        System.out.println("dd");
        for (int i = 7; i >= 1; i--) {
            try {
                System.out.println(i);
               String html =  HttpUtils.get(ip3366 + i);
               System.out.println(html);

                Document doc = Jsoup.parse(html);

                Elements trs = doc.select("div#list > table tbody > tr");

                for (Element tr : trs) {

                    Elements tds = tr.select("td");
                    if ( tds.size()!=7 ) {
                        continue;
                    }

                    String ip = tds.get(0).text();
                    String port = tds.get(1).text();
                    String type = tds.get(3).text();







                    executors.submit(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = null;

                            if ( type.trim().toUpperCase().equals("HTTP") ) {

                                client = builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip.trim(), Integer.parseInt(port))))
                                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                                        .build();

                            } else  if ( type.trim().toUpperCase().equals("HTTPS") ) {
                                client = builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip.trim(), Integer.parseInt(port))))
                                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                                        .build();
                            }

                            if (  client!=null ) {
                                try {
                                    Request request = new Request.Builder()
                                            .url("https://www.grainger.cn")
                                            .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")
                                            .build();

                                    Response response = client.newCall(request).execute();

                                    if ( response.isSuccessful() && response.code()==200 ) {
                                        System.out.println(ip + ":" + port + " - " + type);
                                        System.out.println("\tPASS----------------");

                                        com.gavel.entity.Proxy proxy = new com.gavel.entity.Proxy();
                                        proxy.setId(MD5Utils.md5Hex(ip.trim() + ":" + port.trim()));
                                        proxy.setIp(ip.trim());
                                        proxy.setPort(Integer.parseInt(port));
                                        proxy.setType(type.trim().toUpperCase());
                                        proxy.setUpdatetime(Calendar.getInstance().getTime());

                                        SQLExecutor.insert(proxy);

                                    }
                                } catch (Exception e){
                                    System.out.println("Exception: " + e.getMessage());
                                }


                                client = null;
                            }
                        }
                    });



                }

            } catch (Exception e) {

            }
        }




    }


    public void xiladaili(){
        String xicidaili = "http://www.xiladaili.com/http/";

        for (int i = 100; i >= 1; i--) {
            try {
                String html =  HttpUtils.get(xicidaili + i + "/");

                Document doc = Jsoup.parse(html);

                Elements trs = doc.select("table.fl-table tbody > tr");

                for (Element tr : trs) {

                    Elements tds = tr.select("td");
                    if ( tds.size()!=8 ) {
                        continue;
                    }

                    String host = tds.get(0).text();
                    String type = tds.get(1).text();

                    String ip = host.trim().split(":")[0];
                    String port = host.trim().split(":")[1];


                    final OkHttpClient client = builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip.trim(), Integer.parseInt(port))))
                            .connectTimeout(5000, TimeUnit.MILLISECONDS)
                            .build();

                    executors.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Request request = new Request.Builder()
                                        .url("https://www.grainger.cn")
                                        .header("Host", "www.grainger.com")
                                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")
                                        .build();

                                Response response = client.newCall(request).execute();

                                if ( response.isSuccessful() && response.code()==200 ) {
                                    System.out.println(ip + ":" + port + " - " + type);
                                    System.out.println("\tPASS----------------");

                                    com.gavel.entity.Proxy proxy = new com.gavel.entity.Proxy();
                                    proxy.setId(MD5Utils.md5Hex(ip.trim() + ":" + port.trim()));
                                    proxy.setIp(ip.trim());
                                    proxy.setPort(Integer.parseInt(port));
                                    proxy.setType(type.trim().toUpperCase());
                                    proxy.setUpdatetime(Calendar.getInstance().getTime());

                                    SQLExecutor.insert(proxy);

                                }
                            } catch (Exception e){
                                System.out.println("Exception: " + e.getMessage());
                            }
                        }
                    });

                }

            } catch (Exception e) {

            }
        }
    }

    public void xicidaili(){
        String xicidaili = "https://www.xicidaili.com/nn/";

        for (int i = 1; i <= 746; i++) {
            try {
                String html =  HttpUtils.get(xicidaili + i + "/");

                Document doc = Jsoup.parse(html);

                Elements trs = doc.select("table#ip_list tbody > tr");

                for (Element tr : trs) {

                    Elements tds = tr.select("td");
                    if ( tds.size()!=10 ) {
                        continue;
                    }

                    String ip = tds.get(1).text();
                    String port = tds.get(2).text();
                    String type = tds.get(5).text();





                    executors.submit(new Runnable() {
                        @Override
                        public void run() {

                            OkHttpClient client = null;
                            if ( type.trim().toUpperCase().equals("HTTP") ) {
                                client = builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip.trim(), Integer.parseInt(port))))
                                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                                        .build();

                            } else  if ( type.trim().toUpperCase().equals("HTTPS") ) {
                                client = builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip.trim(), Integer.parseInt(port))))
                                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                                        .build();
                            }

                            if (  client==null ) {
                                return;
                            }

                            try {
                                Request request = new Request.Builder()
                                        .url("https://www.grainger.cn")
                                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")
                                        .build();

                                Response response = client.newCall(request).execute();

                                if ( response.isSuccessful() && response.code()==200 ) {
                                    System.out.println(ip + ":" + port + " - " + type);
                                    System.out.println("\tPASS----------------");

                                    com.gavel.entity.Proxy proxy = new com.gavel.entity.Proxy();
                                    proxy.setId(MD5Utils.md5Hex(ip.trim() + ":" + port.trim()));
                                    proxy.setIp(ip.trim());
                                    proxy.setPort(Integer.parseInt(port));
                                    proxy.setType(type.trim().toUpperCase());
                                    proxy.setUpdatetime(Calendar.getInstance().getTime());

                                    SQLExecutor.insert(proxy);

                                }
                            } catch (Exception e){
                                System.out.println("Exception: " + e.getMessage());
                            }
                        }
                    });




                }

            } catch (Exception e) {

            }
        }
    }

    public static void main(String[] args) {
        ProxyCrawler.getInstance().xiladaili();
    }
}
