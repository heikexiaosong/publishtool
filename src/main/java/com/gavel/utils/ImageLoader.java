package com.gavel.utils;

import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.ImageCache;

import java.io.File;
import java.util.Calendar;

public class ImageLoader {

    public static final String PICS_DIR = "D:\\images";

    public static final String PICS_COMPLETE_DIR = "E:\\images_complete";
    public static final String PICS_TEMP_DIR = "D:\\images\\temp";


    public static ImageCache loadIamge(String url) throws Exception {
       return loadIamge(url, PICS_DIR);
    }

    public static ImageCache loadIamge(String url, String _dir) throws Exception {

        if ( url==null || url.trim().length()==0 ) {
            return null;
        }

        File dir = new File(_dir);
        if ( !dir.exists() ) {
            dir.mkdirs();
        }

        String id = MD5Utils.md5Hex(url.trim());

        ImageCache cache =  SQLExecutor.executeQueryBean("select * from image  where id = ? ", ImageCache.class, id);

        if ( cache != null && cache.getFilepath()!=null && new File(PICS_DIR, cache.getFilepath()).exists() ) {
            //System.out.println("Load Local File: " + new File(PIC_DIR, cache.getFilepath()).getAbsolutePath());
            return cache;
        }

        try {
            String image = url.replace("https://static.grainger.cn/", "").replace("/", File.separator).trim();

            if ( url.equalsIgnoreCase("https://www.grainger.cn/Content/images/hp_np.png") ) {
                image = url.replace("https://www.grainger.cn/", "").replace("/", File.separator).trim();
            }

            File imageFile = new File(_dir + File.separator + image);
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


    /**
     *  获取原始图片
     */
    public static ImageCache loadOrginialIamge(String url) throws Exception {

        if ( StringUtils.isBlank(url.trim()) ) {
            return null;
        }

        String id = MD5Utils.md5Hex(url.trim());
        ImageCache cache =  SQLExecutor.executeQueryBean("select * from image  where id = ? ", ImageCache.class, id);
        if ( cache==null ) {
            cache = new ImageCache();
            cache.setId(id);
            cache.setUrl(url.trim());
            try {
                SQLExecutor.insert(cache);
            } catch (Exception e) {
                System.out.println("[Image Cache]新增失败: " + e.getMessage());
            }
        }

        if ( StringUtils.isNotBlank(cache.getFilepath()) ){
            File image = new File(PICS_DIR, cache.getFilepath());
            if ( image.exists() ) {
                System.out.println("[" + url + "]Load: " + image.getAbsolutePath());
                return cache;
            }
        }

        String image = url.replace("https://static.grainger.cn/", "").replace("/", File.separator).trim();
        if ( url.equalsIgnoreCase("https://www.grainger.cn/Content/images/hp_np.png") ) {
            image = url.replace("https://www.grainger.cn/", "").replace("/", File.separator).trim();
        }

        File imageFile = new File(PICS_DIR + File.separator + image);
        if ( !imageFile.getParentFile().exists() ) {
            imageFile.getParentFile().mkdirs();
        }

        try {
            HttpUtils.download(url, imageFile.getAbsolutePath());
            cache.setFilepath(image);
        } catch (Exception e) {
            System.out.println("[" + url + "]" + e.getMessage());
        }
        cache.setUpdatetime(Calendar.getInstance().getTime());
        try {
            SQLExecutor.update(cache);
        } catch (Exception e) {
            System.out.println("[Image Cache]Update失败: " + e.getMessage());
        }

        return cache;
    }

    public static void main(String[] args) throws Exception {
        loadOrginialIamge("https://static.grainger.cn/pis/CPG/%E9%85%8D%E7%BA%BF%E7%9B%92%E6%96%B9%E5%90%91.jpg");
    }

}
