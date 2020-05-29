package com.gavel;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.HtmlCacheNew;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import com.gavel.utils.ZipUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MongoTest {

    public static void main(String[] args) {

        final  int size = 500;
        int offset = 0;
        while (true) {
            List<HtmlCache> htmlCaches = new ArrayList<>();
            try {
                htmlCaches =  SQLExecutor.executeQueryBeanList(" select * from HTMLCACHE order by URL limit ?, ? ", HtmlCache.class, offset, size);
            } catch (Exception e) {

                e.printStackTrace();
            }

            System.out.println("\n[Offset: " + offset + "]Size: " + htmlCaches.size());

            if ( htmlCaches!=null ) {
                for (int i = 0; i < htmlCaches.size(); i++) {
                    HtmlCache htmlCache =  htmlCaches.get(i);

                    if (StringUtils.isBlank(htmlCache.getHtml())) {
                        continue;
                    }

                    String url = htmlCache.getUrl();

                    url = url.replace("https://www.grainger.cn/","");
                    if ( !url.startsWith("g-") && !url.startsWith("u-") ) {

                        try {
                            SQLExecutor.delete(htmlCache);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        continue;

                    }


                    HtmlCacheNew cache = new HtmlCacheNew();
                    cache.setId(MD5Utils.md5Hex(htmlCache.getUrl().toLowerCase().trim()));
                    cache.setUrl(htmlCache.getUrl());
                    cache.setContentlen(htmlCache.getContentlen());
                    try {
                        cache.setCompress(ZipUtil.compress(htmlCache.getHtml()));
                        SQLExecutor.insert(cache);

                        SQLExecutor.delete(htmlCache);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    System.out.print("\r" + i + "/" + htmlCaches.size() + ": " + htmlCache.getUrl());
                }
            }

            if ( htmlCaches.size() < size ) {
                break;
            }

            offset += size;
        }

    }
}
