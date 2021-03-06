package cv.imgutils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/**
 * Created by chenqiu on 11/26/18.
 */
public abstract class AbstractCVUtils implements SysAsset{

    public Mat loadDebugFile(String fileName){
        return Imgcodecs.imread(System.getProperty(DIR_PROPETY) + DEBUG_DIR + fileName);
    }

    public Mat loadOriginFile(String fileName){
        return Imgcodecs.imread(System.getProperty(DIR_PROPETY) + RELATIVE_DIR + fileName);
    }

    /**
     * <tt>for <strong>origin</strong> image file</tt>
     * @param fileName
     * @return
     */
    public static File newPropertyFile(String fileName){
        return new File(System.getProperty(DIR_PROPETY) + RELATIVE_DIR + fileName);
    }

    /**
     * <tt>for <strong>debug</strong> image file</tt>
     * @param fileName
     * @return
     */
    public static File newPropertyDebugFile(String fileName){
        return new File(System.getProperty(DIR_PROPETY) + DEBUG_DIR + fileName);
    }
    /**
     * 加载绝对路径的图片文件
     * @param absolutePath
     * @return
     */
    public Mat loadFile(String absolutePath){
        return Imgcodecs.imread(absolutePath);
    }

    /**
     * 写入 Mat 文件
     * @param src
     * @param absolutePath absolutePath or file name xxx.jpg etc.
     */
    public static void writeFile(Mat src, String absolutePath){
        // is file name
        if (absolutePath.indexOf('/') == -1){
            absolutePath = System.getProperty(DIR_PROPETY) + DEBUG_DIR + absolutePath;
        }
        Imgcodecs.imwrite(absolutePath, src);
    }

    /**
     * pyrDown method
     * @param src
     * @return
     */
    public Mat normalizeSize(Mat src) {
        final int s = 1200;
        int w = src.width();
        int h = src.height();
        Mat dst = src;
        while (w > s && h > s) {
            System.out.println(src.width() + ", " + src.height());
            Imgproc.pyrDown(src, dst, new Size(w >>= 1, h >>= 1));
            src = dst;
        }
        return dst;
    }

    public static Mat grayToBGR(Mat mat0) {
        Mat color = new Mat(mat0.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat0, color, Imgproc.COLOR_GRAY2BGR);
        return color;
    }

    public Mat pickRectROI(Mat mat0, int n) {return null;};
}
