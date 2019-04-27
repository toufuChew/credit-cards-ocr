import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.bytedeco.javacpp.lept.pixDestroy;

/**
 * Created by chenqiu on 11/16/18.
 */
public class Test {

    public static void main(String []args){

        // 加载
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        // OpenCV Code ...
        Mat src = Imgcodecs.imread("/Users/chenqiu/IdeaProjects/CardIDRecognition/res/img/origin/Credit.jpeg",
                Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        Mat dst = new Mat();

        Imgproc.adaptiveThreshold(src, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY_INV, 31, 5);
        /**
         * //https://my.oschina.net/u/3767256/blog/1802849
         */
        Imgcodecs.imwrite("/Users/chenqiu/IdeaProjects/CardIDRecognition/res/img/debug/Cluster.jpg", dst);
    }
}
