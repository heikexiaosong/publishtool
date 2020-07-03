import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class opencv_version {

  static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

  public static void main(String[] args) {


      System.out.println(Core.getBuildInformation());

      for (int i = 1; i < 5; i++) {
          Mat src = Imgcodecs.imread("E:\\pic\\2017070738.jpg", Imgcodecs.IMREAD_UNCHANGED);

          System.out.println("width: " + src.width());
          System.out.println("height: " + src.height());
          System.out.println("depth: " + src.depth());

          if ( src.width()!=800 || src.height()!=800 ) {
              Imgproc.resize(src, src, new Size(800, 800));
          }

          System.out.println("[" + (src.width()-i) + ", " + ( src.height()-i) + "]" + Arrays.toString(src.get(src.width()-i, src.height()-i)));

          Imgproc.putText(src, Integer.toString(i), new Point(src.width()-20, src.height()-10), 0, 1.0, new Scalar(220,220,220));

          Imgproc.circle(src, new Point(src.width()-i, src.height()-i),  2, new Scalar(238, 238, 238));
          Imgcodecs.imwrite("E:\\pic\\2017070738_" + i + ".jpg", src);
      }




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
