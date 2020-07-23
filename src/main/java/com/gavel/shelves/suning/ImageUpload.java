package com.gavel.shelves.suning;

import com.gavel.HttpUtils;
import com.gavel.config.APPConfig;
import com.gavel.entity.ImageCache;
import com.gavel.utils.ImageLoader;
import com.google.gson.Gson;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.NPicAddRequest;
import com.suning.api.entity.item.NPicAddResponse;
import com.suning.api.entity.item.PicDeleteRequest;
import com.suning.api.entity.item.PicDeleteResponse;
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
        request.setPicFileData("D:\\pics\\餐具套装_1595488707010\\65481123170_1.jpg");
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

                for (int i = 0; i < 3; i++) {
                    Thread.sleep(10000);

                    long len =   HttpUtils.imageLength(response.getSnbody().getAddNPic().getPicUrl() + "?version=1");
                    System.out.println(len);

                    if ( len < 999 ) {
                        System.out.println("待删除...");
                        PicDeleteRequest deleteRequest = new PicDeleteRequest();
                        deleteRequest.setPicUrl(response.getSnbody().getAddNPic().getPicUrl());

                        PicDeleteResponse _response = APPConfig.getInstance().client().excute(deleteRequest);
                        System.out.println(new Gson().toJson(_response));
                    }
                }


            }
        } catch (SuningApiException e) {
            e.printStackTrace();
        }

        // http://uimgproxy.suning.cn/uimg1/sop/commodity/CM9oEx8ei2AETvR1mr0BlQ.jpg
        // http://uimgproxy.suning.cn/uimg1/sop/commodity/cDjuTGkwxNhX1JIHNzxKBw.jpg
        // http://uimgproxy.suning.cn/uimg1/sop/commodity/Aa4dczkMYROvl-xuwfQVeA.jpg

    }
}
