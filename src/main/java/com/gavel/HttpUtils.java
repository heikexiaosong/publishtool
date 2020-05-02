package com.gavel;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Proxy;
import com.google.common.io.Files;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


public class HttpUtils {

	public final  static List<Cookie> cache = new ArrayList<>();

    private static final OkHttpClient client = new OkHttpClient.Builder()
													.connectTimeout(15, TimeUnit.SECONDS)
													.readTimeout(15, TimeUnit.MINUTES)
													.cookieJar(new CookieJar() {
														@Override
														public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
															//内存中缓存Cookie
															cache.addAll(list);
														}

														@Override
														public List<Cookie> loadForRequest(HttpUrl url) {
															//过期的Cookie
															List<Cookie> invalidCookies = new ArrayList<>();
															//有效的Cookie
															List<Cookie> validCookies = new ArrayList<>();

															for (Cookie cookie : cache) {

																if (cookie.expiresAt() < System.currentTimeMillis()) {
																	//判断是否过期
																	invalidCookies.add(cookie);
																} else if (cookie.matches(url)) {
																	//匹配Cookie对应url
																	validCookies.add(cookie);
																}
															}

															//缓存中移除过期的Cookie
															cache.removeAll(invalidCookies);

															//返回List<Cookie>让Request进行设置
															return validCookies;
														}
													})
    												.build();

    public static final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, lko) Chrome/81.0.4044.129 Safari/537.36";


    private static final LinkedBlockingDeque<Proxy> proxyQueue = new LinkedBlockingDeque<>();


    static {

		try {
			List<Proxy>	 proxies = SQLExecutor.executeQueryBeanList("select * from proxy ", Proxy.class);
			if ( proxies!=null && proxies.size()>0 ) {
				proxyQueue.addAll(proxies);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

    /**
     * get请求
     * @param url
     * @return
     */
    public static String get(String url) {

    	int tryTimes = 0;

		Proxy proxy = proxyQueue.poll();

		Request request = new Request.Builder()
				.url(url)
				.header("User-Agent", USERAGENT)
				.build();

		OkHttpClient httpClient = client;
		if ( proxy!=null ) {
			httpClient = new OkHttpClient.Builder()
					.proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getIp(),proxy.getPort())))
					.build();
		}

        Response response = null;
        while ( true ) {
        	if ( tryTimes > 0 ) {
				System.out.println("第 " + tryTimes + " 次重试...");
			}
			try {
				response = httpClient.newCall(request).execute();
				if (!response.isSuccessful())
					throw new RuntimeException("请求失败： " + response);
				return response.body().string();

			} catch (Exception e) {
				tryTimes++;
				if ( tryTimes > 3 ) {
					System.out.println(e.getMessage());
					return "<html />";
				} else {
					System.out.println(response.code() + " - "  + url);
					System.out.println(response.message());
					try {
						Thread.sleep(tryTimes*5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}finally {
				proxyQueue.push(proxy);
				if (response != null) {
					response.close();
				}
			}
		}

    }

	/**
	 * get请求
	 * @param url
	 * @return
	 */
	public static String get(String url, OkHttpClient okHttpClient) {
		Response response = null;
		try {
			Request request = new Request.Builder()
					.url(url)
					.header("User-Agent", USERAGENT)
					.build();
			response = okHttpClient.newCall(request).execute();
			if (!response.isSuccessful())
				throw new RuntimeException("请求失败： " + response);
			return response.body().string();

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}finally {
			if (response != null) {
				response.close();
			}
		}
	}

	/**
	 * get请求
	 * @param url
	 * @return
	 */
	public static String get(String url, String referer) {
		Response response = null;
		try {
			Request request = new Request.Builder()
					.url(url)
					.header("Referer", referer)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")
					.header("Cookie", "_ga=GA1.2.789327771.1587133742; _gid=GA1.2.508946531.1587133743; __RequestVerificationToken=5QJRS4KOpfUgHhHCCGd5spBzZf_cYKBH5Xqklup5bAVb8SbhfcDgx_KvV5S5IPw3O-NHbw2; Hm_lvt_69f75b7843fe943150d92e384dc03fef=1587381810,1587430746,1587440869,1587453801; _gat_gtag_UA_120067393_1=1; lastview=305353,649793,318921,405775,659846; Hm_lpvt_69f75b7843fe943150d92e384dc03fef=1587474108")
					.build();
			response = client.newCall(request).execute();
			if (!response.isSuccessful())
				throw new RuntimeException("请求失败： " + response);
			return response.body().string();

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}finally {
			if (response != null) {
				response.body().close();
				response.close();
			}
		}
	}

	/**
	 * post请求
	 * @param url
	 * @param params
	 * @return
	 */
	public static String post(String url, Map<String, String> params){
		Response response = null;
		try {

			FormBody.Builder builder = new FormBody.Builder();
			for (String key : params.keySet()) {
				if ( key!=null && params.get(key)!= null ) {
					builder.add(key.trim(), params.get(key));
				}
			}

			Request request = new Request.Builder()
					.url(url)
					.post(builder.build())
					.build();
			response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				System.out.println("请求失败： " + response.body().string());
				throw new RuntimeException("请求失败： " + response.message());
			}
			String responseText = response.body().string();
			return responseText;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		//1.创建OkHttpClient对象
		OkHttpClient okHttpClient = new OkHttpClient();
		String url = "https://static.grainger.cn/product_images_new/800/1H8/12991_1_9591.jpg";

		//2.创建Request对象
		Request request  = new Request.Builder()
				.url(url)
				.build();

		//3.异步请求newCall（Callback）
		Call call = okHttpClient.newCall(request);


		Response response = call.execute();
		if ( response.isSuccessful() ) {

			Files.write(response.body().bytes(), new File("test.jpg"));

		}



	}


	public static void download(String url, String localFilePath) throws IOException {

		//2.创建Request对象
		Request request  = new Request.Builder()
				.url(url)
				.build();

		//3.异步请求newCall（Callback）
		Call call = client.newCall(request);


		Response response = call.execute();
		if ( response.isSuccessful() ) {

			Files.write(response.body().bytes(), new File(localFilePath));

		}

	}
}
