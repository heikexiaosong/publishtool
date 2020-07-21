package com.gavel.shelves.suning;

import com.gavel.config.APPConfig;
import com.gavel.entity.ImageCache;
import com.gavel.utils.ImageLoader;
import com.google.gson.Gson;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.NPicAddRequest;
import com.suning.api.entity.item.NPicAddResponse;
import com.suning.api.exception.SuningApiException;
import org.opencv.core.Core;

import java.util.Calendar;

public class ImageUpload {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static String execute(String url) throws Exception {

        ImageCache image = ImageLoader.loadOrginialIamge(url);


        return image.getFilepath();
    }


    public static void main(String[] args) throws Exception {

//       String url = execute("http://img30.360buyimg.com/popWaterMark/jfs/t1/96763/32/16831/116382/5e7ee0bbE42a76d3d/4b147f363759eb81.jpg");
//
//        System.out.println("Img: " + url);

        System.out.println(APPConfig.getInstance().getShopinfo().getName());

        NPicAddRequest request = new NPicAddRequest();
        request.setPicFileData("D:\\images\\popWaterMark\\jfs\\t1\\62547\\19\\2596\\4754972\\5d0db049E68b74752\\4eacb828fbd632e1.gif");
        try {
            //System.out.println(request.getResParams());
            System.out.println("当前时间: " + Calendar.getInstance().getTime());
            NPicAddResponse response = APPConfig.getInstance().client().excuteMultiPart(request);
            SuningResponse.SnError error = response.getSnerror();
            if ( error!=null ) {
                System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                System.out.println(new Gson().toJson(response));
            } else {
                System.out.println(new Gson().toJson(response.getSnbody().getAddNPic()));
            }
        } catch (SuningApiException e) {
            e.printStackTrace();
        }

    }
}
