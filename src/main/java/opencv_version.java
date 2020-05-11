import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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


      Mat src = Imgcodecs.imread("D:\\pics\\1-2.jpg", Imgcodecs.IMREAD_UNCHANGED);

      Mat logo = Imgcodecs.imread("D:\\pics\\js.png", Imgcodecs.IMREAD_UNCHANGED);


    Mat imgHSV = new Mat(logo.rows(), logo.cols(), CvType.CV_8UC3);
    //RGB->HSV
    Imgproc.cvtColor(logo, imgHSV, Imgproc.COLOR_BGR2GRAY);
    Scalar minValues = new Scalar(0, 0, 0);
    Scalar maxValues = new Scalar(180, 255, 46);
    Mat mask = new Mat();
    Core.inRange(imgHSV, minValues, maxValues, mask);
    Mat blackImg = new Mat();
    Core.bitwise_and(logo, imgHSV, blackImg);


    Imgcodecs.imwrite("D:\\pics\\js_1.png", logo);


  }

}
