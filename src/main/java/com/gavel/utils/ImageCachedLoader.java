package com.gavel.utils;

import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.ImageCache;

import java.io.File;
import java.util.Calendar;

public class ImageCachedLoader {

    public static final String PICS_DIR = "D:\\images";


    public static ImageCache loadIamge(String url) throws Exception {

        if ( url==null || url.trim().length()==0 ) {
            return null;
        }

        File dir = new File(PICS_DIR);
        if ( !dir.exists() ) {
            dir.mkdirs();
        }

        String id = MD5Utils.md5Hex(url.trim());
        ImageCache cache =  SQLExecutor.executeQueryBean("select * from image  where id = ? ", ImageCache.class, id);

        if ( cache != null && cache.getFilepath()!=null && new File(dir, cache.getFilepath()).exists() ) {
            return cache;
        }

        try {
            String image = url.replace("https://static.grainger.cn/", "").replace("/", File.separator).trim();
            if ( url.equalsIgnoreCase("https://www.grainger.cn/Content/images/hp_np.png") ) {
                image = url.replace("https://www.grainger.cn/", "").replace("/", File.separator).trim();
            }

            File imageFile = new File(dir, image);
            if ( !imageFile.getParentFile().exists() ) {
                imageFile.getParentFile().mkdirs();
            }

            HttpUtils.download(url, imageFile.getAbsolutePath());

            if ( cache == null ){
                cache = new ImageCache();
                cache.setId(id);
                cache.setUrl(url.trim());
                cache.setFilepath(image);
                cache.setUpdatetime(Calendar.getInstance().getTime());
                SQLExecutor.insert(cache);
                return cache;
            } else {
                cache.setUrl(url.trim());
                cache.setFilepath(image);
                cache.setUpdatetime(Calendar.getInstance().getTime());
                SQLExecutor.update(cache);
                return cache;
            }

        } catch (Exception e) {
            System.out.println("下载图片错误: " + e.getMessage());
        }

        return cache;
    }

}
