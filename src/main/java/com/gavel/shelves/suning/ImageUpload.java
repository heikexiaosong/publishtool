package com.gavel.shelves.suning;

import com.gavel.entity.ImageCache;
import com.gavel.utils.ImageLoader;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class ImageUpload {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static String execute(String url) throws Exception {

        ImageCache image = ImageLoader.loadOrginialIamge(url);


        return image.getFilepath();
    }


    public static void main(String[] args) throws Exception {

        File imageFile = new File("D:\\pics\\1595554002742", "10020179543659_1.jpg");

        Mat src = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
        if ( src.width()!=800 || src.height()!=800 ) {
            Imgproc.resize(src, src, new Size(800, 800));
        }

        double[] pixes = src.get(src.width()-30, src.height()-10);
        Scalar scalar =  new Scalar(238,  238,  238);
        if ( pixes!=null && pixes.length==3 ) {
            pixes[0] = pixes[0]-15;
            pixes[1] = pixes[1]-15;
            pixes[2] = pixes[2]-15;
            scalar = new Scalar(pixes);
        }

        Imgproc.putText(src, Integer.toString(1), new Point(src.width()-30, src.height()-10), 0, 1.0, scalar);
        Imgcodecs.imwrite(imageFile.getAbsolutePath(), src);

    }
}
