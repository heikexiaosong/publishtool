package com.gavel.proxy;

import com.gavel.entity.Proxy;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class XiladailiLoader implements ProxyLoader {

    private static final String xicidaili = "http://www.xiladaili.com/http/";

    @Override
    public List<Proxy> collection() {

        List<Proxy> proxies = new ArrayList<>();


        try {
            BufferedReader reader = Files.newReader(new File("ips"), Charset.forName("UTF8"));

            String line = reader.readLine();
            while ( line!=null ) {
                System.out.println(line);

                String ip = line.trim().split(":")[0];
                String port = line.trim().split(":")[1];

                Proxy proxy = new Proxy();
                proxy.setIp(ip);
                proxy.setPort(Integer.parseInt(port));
                proxy.setType("HTTP");

                proxies.add(proxy);


                line = reader.readLine();
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return proxies;
    }
}
