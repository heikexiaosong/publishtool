package com.gavel.utils;

import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.ImageCache;

import java.io.File;
import java.util.Calendar;

public class ImageLoader {

    public static final String PIC_DIR = "D:\\images";

    public static ImageCache loadIamge(String url) throws Exception {

        if ( url==null || url.trim().length()==0 ) {
            return null;
        }

        File dir = new File(PIC_DIR);
        if ( !dir.exists() ) {
            dir.mkdirs();
        }

        String id = MD5Utils.md5Hex(url.trim());

        ImageCache cache =  SQLExecutor.executeQueryBean("select * from image  where id = ? ", ImageCache.class, id);

        if ( cache != null && cache.getFilepath()!=null && new File(PIC_DIR, cache.getFilepath()).exists() ) {
            System.out.println("Load Local File: " + new File(PIC_DIR, cache.getFilepath()).getAbsolutePath());
            return cache;
        }

        if ( cache == null ){
            cache = new ImageCache();
        }

        try {
            String image = url.replace("https://static.grainger.cn/", "").replace("/", File.separator).trim();

            File imageFile = new File(PIC_DIR + File.separator + image);
            if ( !imageFile.getParentFile().exists() ) {
                imageFile.getParentFile().mkdirs();
            }


            HttpUtils.download(url, imageFile.getAbsolutePath());
            System.out.println("Load Local Network");
            cache.setId(id);
            cache.setUrl(url.trim());
            cache.setFilepath(image);
            cache.setUpdatetime(Calendar.getInstance().getTime());
            SQLExecutor.insert(cache);
            return cache;
        } catch (Exception e) {
            System.out.println("下载图片错误: " + e.getMessage());
        }

        return cache;
    }

}
