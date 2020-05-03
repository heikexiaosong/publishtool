package com.gavel.proxy;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 爬取代理并检测代理是否存活
 */
public class ProxyServer {

    private final ExecutorService executors = Executors.newFixedThreadPool(20);

    private static final ProxyServer instance = new ProxyServer();

    private ProxyServer() {
    }

    public static ProxyServer getInstance() {
        return instance;
    }


    private final List<ProxyLoader> loaders = new ArrayList<>();

    public void reg(ProxyLoader loader){

        if ( loader==null ) {
            return;
        }

        for (ProxyLoader proxyLoader : loaders) {
            if ( proxyLoader.getClass().equals(loader.getClass()) ) {
                return;
            }
        }

        loaders.add(loader);
    }

    public void start(){

        Request request = new Request.Builder()
                .url("http://119.3.92.249:8099/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")
                .build();



        if ( loaders!=null && loaders.size()>0 ) {

            for (ProxyLoader loader : loaders) {
                List<com.gavel.entity.Proxy> proxys = loader.collection();

                System.out.println("\n------------ " + loader.getClass().getName() + "--------------------");
                int i = 0;
                for (com.gavel.entity.Proxy proxy : proxys) {
                    System.out.print("\r" + (i++) + ". " +  proxy.getIp() + ":" + proxy.getPort());

                    try {
                        OkHttpClient client = new OkHttpClient.Builder()
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getIp(), proxy.getPort())))
                                .connectTimeout(2000, TimeUnit.MILLISECONDS)
                                .readTimeout(1000, TimeUnit.MILLISECONDS)
                                .build();

                        Response response = client.newCall(request).execute();
                        System.out.println("[" +  response.code()  +"]Successful: " +response.isSuccessful());
                    } catch (Exception e) {
                        System.out.print("  ----------------   Msg: " + e.getMessage());
                    } finally {

                    }
                }
            }
        }
    }



    public static void main(String[] args) {
        getInstance().reg(new XiladailiLoader());
            getInstance().start();
    }
}
