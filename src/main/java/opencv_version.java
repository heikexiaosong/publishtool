import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

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


    Imgcodecs.imwrite("D:\\pics\\js_1.png", src);


  }

}
