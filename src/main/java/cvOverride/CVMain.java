package cvOverride;

import cvImgUtil.SysAsset;
import debug.Debug;
import imgToText.TessReg;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import static java.lang.System.exit;
import static org.opencv.imgproc.Imgproc.resize;

/**
 * Created by chenqiu on 12/9/18.
 */
public class CVMain {

    static void canny() {
        Mat m = CVGrayTransfer.grayTransferBeforeScale("Credit.jpg", false);
        Imgproc.GaussianBlur(m, m, new Size(13, 13), 0);
        Imgproc.Canny(m, m, 300, 600, 5, true);
        Imgproc.dilate(m, m, new Mat(), new Point(-1, -1), 1);
        CVCluster.helper.writeFile(m, SysAsset.debugPath() + "Canny.jpg");
        exit(1);
    }

    public static void main(String []args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(TessReg.getOCRText(Debug.newPropertyDebugFile("Cluster_Rect_Area_E.jpg")));
        String fileName = "E.jpg";
        Debug.s();

        Mat src = CVGrayTransfer.grayTransferBeforeScale(fileName, false);
        // debug
        Mat hat = new Mat();
        Imgproc.morphologyEx(src, hat, Imgproc.MORPH_TOPHAT, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 3)));

        Debug.log("width: " + src.width());
        Debug.log("height: " + src.height());
        CVCluster cvCluster = new CVCluster();
        Mat bin = cvCluster.cluster(src);
        Debug.log("cluster end >>>");
        CVCluster.helper.writeFile(bin, System.getProperty(CVCluster.helper.DIR_PROPETY) + CVCluster.helper.RELATIVE_DIR + "resize.jpg");

        Mat dilate = new Mat();
        Size slight = new Size(10, 10);
        Size heavy = new Size(35, 13);
        Imgproc.dilate(bin, dilate, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, heavy));
        Debug.e();
        /**
         * close opr
         * clear small gap among dilate area
         * Imgproc.morphologyEx(dilate, dilate, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10, 8)));
         */
        Imgproc.morphologyEx(dilate, dilate, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10, 1)));
        CVCluster.helper.writeFile(dilate, "Cluster_Erode_" + fileName.substring(0, fileName.indexOf('.')) + ".jpg");

        Mat out = CVRegion.drawRectangle(dilate, new RectFilter() {
            @Override
            public boolean isDigitRegion(Rect rect, int srcWidth, int srcHeight) {
                return true;
            }
            @Override
            public int IDRegionSimilarity(Rect roi, int rows, int cols) {
                return 0;
            }
            @Override
            public Rect findMaxRect(Mat m) {
                return null;
            }
        });
        // draw all rect
        CVCluster.helper.writeFile(out, SysAsset.debugPath() + "Cluster_Rect_Area_" + fileName.substring(0, fileName.indexOf('.')) + ".jpg");

        // bounding id region
        src = CVGrayTransfer.grayTransferBeforeScale(fileName, false);
        CVRegion cvRegion = new CVRegion(src);
        cvRegion.digitRegion(dilate);
//        dilate = CVRegion.drawRectRegion(dilate, cvRegion.rectOfDigitRow);
        cvRegion.digitSeparate(cvCluster.type);
        CVCluster.helper.writeFile(cvRegion.getMatListOfDigit().get(0), SysAsset.debugPath() + "Cluster_Rect_Area_" + fileName.substring(0, fileName.indexOf('.')) + ".jpg");
//        CVCluster.helper.writeFile(cvRegion.getMatListOfDigit().get(1), SysAsset.debugPath() + "Cluster.jpg");
    }
}
