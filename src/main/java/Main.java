import com.gavel.database.SQLExecutor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {

        List<com.gavel.entity.Proxy> proxys = SQLExecutor.executeQueryBeanList("select * from proxy", com.gavel.entity.Proxy.class);

        Request request = new Request.Builder()
                .url("https://www.baidu.com/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")
                .build();

        for (com.gavel.entity.Proxy proxy : proxys) {
            System.out.println(proxy.getIp() + ": " + proxy.getPort());

             try {
                 OkHttpClient client = new OkHttpClient.Builder()
                         .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getIp(), proxy.getPort())))
                         .connectTimeout(3000, TimeUnit.MILLISECONDS)
                         .build();



                 Response response = client.newCall(request).execute();

                 System.out.println("[" +  response.code()  +"]Successful: " +response.isSuccessful());
             } catch (Exception e) {
                 System.out.println("Msg: " + e.getMessage());
             } finally {

             }

        }



    }
}
