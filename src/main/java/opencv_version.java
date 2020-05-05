import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class opencv_version {

  static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

  public static void main(String[] args) {

    if ((1==args.length) && (0==args[0].compareTo("--build"))) {

        System.out.println(Core.getBuildInformation());
    } else
    if ((1==args.length) && (0==args[0].compareTo("--help"))) {

        System.out.println("\t--build\n\t\tprint complete build info");
        System.out.println("\t--help\n\t\tprint this help");
    } else {

        System.out.println("Welcome to OpenCV " + Core.VERSION);
    }


      Mat large = Imgcodecs.imread("D:\\pics\\2016040516_800.jpg", Imgcodecs.IMREAD_UNCHANGED);

      Mat small = Imgcodecs.imread("D:\\pics\\2016040516_350.jpg", Imgcodecs.IMREAD_UNCHANGED);


      Mat dest = small.clone();

      Imgproc.resize(small, dest, new Size(800, 800));


      System.out.println(small.type() + ": " + small.channels());



      System.out.println(Arrays.toString(large.get(200, 550)));
    System.out.println(Arrays.toString(large.get(262, 252)));
    System.out.println(Arrays.toString(large.get(110,65)));

      Imgcodecs.imwrite("D:\\pics\\2016040516_350_1.png", dest);


      Mat mat1_gray = new Mat();
      Imgproc.cvtColor(large, mat1_gray, Imgproc.COLOR_BGR2GRAY);
      Mat mat2_gray = new Mat();
      Imgproc.cvtColor(dest, mat2_gray, Imgproc.COLOR_BGR2GRAY);


      Mat mat_result = new Mat();
      Core.absdiff(mat1_gray, mat2_gray, mat_result);

     //将灰度图按照阈值进行绝对值化
    mat_result.convertTo(mat_result, CvType.CV_8UC1);
    List<MatOfPoint> mat2_list = new ArrayList<MatOfPoint>();
    Mat mat2_hi = new Mat();
    //寻找轮廓图
    Imgproc.findContours(mat_result, mat2_list, mat2_hi, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
    Mat mat_result1 = large;
    Mat mat_result2 = dest;
    //使用红色标记不同点
    System.out.println(mat2_list.size());
    for (MatOfPoint matOfPoint : mat2_list)
    {
      Rect rect = Imgproc.boundingRect(matOfPoint);
      Imgproc.rectangle(mat_result1, rect.tl(), rect.br(), new Scalar(0, 0, 255),2);
      Imgproc.rectangle(mat_result2, rect.tl(), rect.br(), new Scalar(0, 0, 255),2);
    }

    Imgcodecs.imwrite("D:\\pics\\res_1.png", mat_result1);


    Imgcodecs.imwrite("D:\\pics\\res_2.png", mat_result2);


  }

}
