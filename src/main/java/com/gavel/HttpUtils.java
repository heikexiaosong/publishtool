package com.gavel;

import okhttp3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    /**
     * get请求
     * @param url
     * @return
     */
    public static String get(String url) {
        Response response = null;
    	try {
    		Request request = new Request.Builder()
										.url(url)
										.build();
    		response = client.newCall(request).execute();
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
	public static String get(String url, OkHttpClient okHttpClient) {
		Response response = null;
		try {
			Request request = new Request.Builder()
					.url(url)
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
					.build();
			response = client.newCall(request).execute();
			System.out.println("response[" + response.code() + "]: " + response.body());
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


}
