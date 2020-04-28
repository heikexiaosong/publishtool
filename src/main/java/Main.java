import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.GraingerCategory;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.ImageCache;
import com.gavel.grainger.GraingerProductLoad;
import com.gavel.utils.ImageLoader;
import com.gavel.utils.MD5Utils;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public class Main {

    private static final String PIC_DIR = "D:\\images";

    public static void main(String[] args) throws Exception {

        ImageCache cache = ImageLoader.loadIamge("https://static.grainger.cn/product_images_new/800/1D6/1198_1_7295.jpg");
//
//        cache.setPicurl("1234");
//
//        SQLExecutor.update(cache);

    }
}
