package com.gavel.proxy;

import com.google.common.io.Files;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class HttpProxyClient {


    private static final OkHttpClient DEFAULT_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private static final HttpProxyClient INSTANCE = new HttpProxyClient();

    private  final ExecutorService checkExecutors;

    private  final ScheduledExecutorService cronService;

    private final List<OkHttpClient> httpClients;

    private HttpProxyClient() {
        this.checkExecutors = Executors.newFixedThreadPool(10);
        this.cronService = Executors.newSingleThreadScheduledExecutor();
        this.httpClients = new CopyOnWriteArrayList<>();

        check();

        cronService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("开始检测。。。");
                check();
            }
        }, 60*1000, 60*1000, TimeUnit.MILLISECONDS);

    }

    private void check() {
        Request request = new Request.Builder()
                .url("http://119.3.92.249:8099/pub/test.txt")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")
                .build();


        List<String> lines = null;
        try {
            lines = Files.readLines(new File("ips"), Charset.forName("UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
            lines = Collections.EMPTY_LIST;
        }

        List<Future> futures = new ArrayList<>();
        System.out.println("IPS: " + lines.size());
        for (String line : lines) {
            final String ip = line.trim().split(":")[0];
            final int port = Integer.parseInt(line.trim().split(":")[1]);

            Future<OkHttpClient> future = checkExecutors.submit(new Callable<OkHttpClient>() {
                @Override
                public OkHttpClient call() throws Exception {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(ip, port)))
                            .connectTimeout(3000, TimeUnit.MILLISECONDS)
                            .readTimeout(2000, TimeUnit.MILLISECONDS)
                            .build();

                    Response response = null;
                    try {
                        response = client.newCall(request).execute();
                        if ( !response.isSuccessful() ) {
                            throw new RuntimeException("[Code: " + response.code() + "]" + response.message());
                        }

                        String msg = response.body().string();
                        if ( response.code()==200 && "200".equalsIgnoreCase(msg.substring(0, 3)) ) {
                            return client;
                        }
                    } catch (Exception e) {

                    } finally {
                        if ( response!=null ) {
                            response.close();
                            response = null;
                        }
                    }
                    return null;
                }
            });
            futures.add(future);
        }

        List<OkHttpClient> _httpClients = new ArrayList<>();
        for (Future<OkHttpClient> future : futures) {
            try {
                OkHttpClient client = future.get();
                if ( client!=null ) {
                    _httpClients.add(client);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        httpClients.clear();
        httpClients.addAll(_httpClients);
        System.out.println("有效的代理: " + httpClients.size());
    }

    public static HttpProxyClient getInstance() {
        return INSTANCE;
    }

    public OkHttpClient defaultClient() {
        return DEFAULT_CLIENT;
    }

    public OkHttpClient getClient() {
        if ( httpClients==null || httpClients.size()==0 ) {
            return null;
        }
        return httpClients.remove(0);
    }

    public void release(OkHttpClient client) {
        if ( client==null || DEFAULT_CLIENT.equals(client) ) {
            return ;
        }

        httpClients.add(client);
    }
}
