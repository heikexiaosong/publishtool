import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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


      Mat src = Imgcodecs.imread("E:\\images_complete\\product_images_new\\800\\5T4\\2017070515.jpg", Imgcodecs.IMREAD_UNCHANGED);


      Imgproc.putText(src, String.valueOf(1), new Point(720,750),Imgproc.FONT_HERSHEY_PLAIN, 2.0, new Scalar(238, 238, 238),4, Imgproc.LINE_AA,false);


      Imgcodecs.imwrite("D:\\pics\\js_1.png", src);


  }

}
