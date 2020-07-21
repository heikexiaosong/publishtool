package com.gavel.shelves.suning;

import com.gavel.entity.ImageCache;
import com.gavel.utils.ImageLoader;
import org.opencv.core.Core;

public class ImageUpload {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static String execute(String url) throws Exception {

        ImageCache image = ImageLoader.loadOrginialIamge(url);


        return image.getFilepath();
    }


    public static void main(String[] args) throws Exception {

       String url = execute("http://img30.360buyimg.com/popWaterMark/jfs/t1/96763/32/16831/116382/5e7ee0bbE42a76d3d/4b147f363759eb81.jpg");

        System.out.println("Img: " + url);

    }
}
