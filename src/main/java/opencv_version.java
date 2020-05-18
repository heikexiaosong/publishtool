import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
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


      Mat pic = Imgcodecs.imread("D:\\pics\\src.jpg", Imgcodecs.IMREAD_UNCHANGED);

      Mat logo_org = Imgcodecs.imread("D:\\pics\\jingsu.png", Imgcodecs.IMREAD_UNCHANGED);


      Mat src  = convertTo4ChannelAndReleaseOld(pic);


      Mat logo = logo_org.clone();

      Imgproc.resize(logo_org, logo, new Size(220, 110));

      Imgcodecs.imwrite("D:\\pics\\jingsu_1.png", logo);


      byte[] data1 = new byte[src.rows() * src.cols() * (int) (src.elemSize())];
      src.get(0, 0, data1);


      Mat roi = new Mat(src, new Range(logo.width(), src.height()));



      System.out.println(src.channels());
      System.out.println(logo.channels());


      Mat mix = roi.clone();
      double alpha = 0.75;  //设置的人物原图的权重（透明度）
      double beta = 1.0-alpha;  //用来叠加的背景的权重

      Core.addWeighted(roi, alpha, logo, beta, 0, mix);

      Imgcodecs.imwrite("D:\\pics\\src_mask.png", mix);


  }

    public static Mat convertTo4ChannelAndReleaseOld(Mat inputPicMat){
        if( inputPicMat.channels() == 3 ){
            List<Mat> srcList=new ArrayList<Mat>();
            List<Mat> dstList=new ArrayList<Mat>();

            Mat convertedPicMat = new Mat(inputPicMat.size(), CvType.makeType(inputPicMat.depth(),4));

            int[] from_to = { 0,0, 1,1, 2,2, 2,3 };
            MatOfInt fromto = new MatOfInt(from_to);

            srcList.add(0,inputPicMat);
            dstList.add(0,convertedPicMat);
            Core.mixChannels(srcList,dstList, fromto);
            inputPicMat.release();
            return convertedPicMat;
        }else if (inputPicMat.channels() == 1){
            List<Mat> srcList=new ArrayList<Mat>();
            List<Mat> dstList=new ArrayList<Mat>();

            Mat convertedPicMat = new Mat(inputPicMat.size(), CvType.makeType(inputPicMat.depth(),4));

            int[] from_to = { 0,0, 0,1, 0,2, 0,3 };
            MatOfInt fromto = new MatOfInt(from_to);

            srcList.add(0,inputPicMat);
            dstList.add(0,convertedPicMat);
            Core.mixChannels(srcList,dstList, fromto);
            inputPicMat.release();
            return convertedPicMat;
        }else{
            return inputPicMat;
        }
    }

}
