import com.gavel.database.SQLExecutor;
import com.gavel.entity.Brand;
import com.gavel.entity.BrandMapping;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {


        OkHttpClient client = new OkHttpClient.Builder()
                                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("223.71.203.241", 55443)))
                                                .build();

        Request request = new Request.Builder()
                .url("https://www.baidu.com/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println("Successful: " +response.isSuccessful());

        System.out.println(response.code());

        System.out.println(response.body().string());

    }
}
