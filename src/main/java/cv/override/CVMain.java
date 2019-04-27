package cv.override;

import cv.imgutils.RectFilter;
import cv.imgutils.SysAsset;
import debug.Debug;
import imgToText.TessReg;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import static java.lang.System.exit;
import static org.opencv.imgproc.Imgproc.*;

/**
 * Created by chenqiu on 12/9/18.
 */
public class CVMain {
    static void canny() {
        Mat m = CVCluster.helper.loadDebugFile("Cluster.jpg");
        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
//        m = CVDilate.fastDilate(m, false);
//        Imgproc.morphologyEx(m, m, Imgproc.MORPH_TOPHAT, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(18, 10)));
        Mat dst = new Mat();
        Imgproc.Sobel(m, dst, CvType.CV_32F, 1, 0, -1);
        m = dst;
//        Imgproc.GaussianBlur(m, m, new Size(13, 13), 0);
//        Imgproc.Canny(m, m, 300, 600, 5, true);

//        Imgproc.morphologyEx(m, m, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
//        Imgproc.threshold(m, m, 0, 255, THRESH_BINARY | Imgproc.THRESH_OTSU);
//        Imgproc.medianBlur(m, m, 3);
//
//        Imgproc.dilate(m, m, new Mat(), new Point(-1, -1), 1);
//        Size heavy = new Size(5, 5);
//        Imgproc.dilate(m, m, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, heavy));
        CVCluster.helper.writeFile(m, SysAsset.debugPath() + "Canny.jpg");
        exit(1);
    }


    static void canny2() {
        String fileName = "C.jpg";
        Mat gray = CVGrayTransfer.grayTransferBeforeScale(fileName, false);
        Debug.log("gray.width = " + gray.cols() + ", gray.height = " + gray.rows());
        boolean findBright = false;
        CVRegion cvRegion = new CVRegion(gray);
        Rect bestRect = new Rect();
        final float fullWidth = gray.cols() - cvRegion.border * 2;
        for ( ; ; findBright = true) {
            Mat dilate = CVDilate.fastDilate(gray, findBright);
            CVCluster.helper.writeFile(dilate, SysAsset.debugPath() + "Cluster_Erode_" + fileName.substring(0, fileName.indexOf('.')) + ".jpg");
            // bounding id region
            Rect idRect = null;
            try {
                idRect = cvRegion.digitRegion(dilate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Debug.log("idRect = " + idRect);

            Mat out = CVRegion.drawRectangle(dilate, new RectFilter() {
                @Override
                public boolean isDigitRegion(Rect rect, int srcWidth, int srcHeight) {
                    return true;
                }

                @Override
                public int IDRegionSimilarity(Mat m, Rect roi, int rows, int cols) {
                    return 0;
                }

                @Override
                public void findMaxRect(Mat m, Rect roi) {}
            });
            // draw all rect
            CVCluster.helper.writeFile(out, SysAsset.debugPath() + "Cluster_Rect_Area_" + fileName.substring(0, fileName.indexOf('.')) + ".jpg");

            if (idRect != null) {
                boolean chose = false;
                if (bestRect.width == 0)
                    chose = true;
                else if (idRect.width < fullWidth) {
                    if (bestRect.width == fullWidth)
                        chose = true;
                    else if (idRect.width > bestRect.width)
                        chose = true;
                }
                if (chose) {
                    bestRect = idRect;
                    Debug.log("findBright= " + findBright);
                }
            }
            if (findBright) break;
        }
        if (bestRect.width == 0) {
            System.err.println("OCR Failed.");
            exit(1);
        }
        cvRegion.setRectOfDigitRow(bestRect);
        try {
            cvRegion.digitSeparate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        CVCluster.helper.writeFile(new Mat(gray, bestRect), SysAsset.debugPath() + "Cluster.jpg");
    }

    public static void main(String []args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(TessReg.getOCRText(Debug.newPropertyDebugFile("Cluster_Rect_Area_E.jpg")));
        Debug.s();
//        canny();
        canny2();
//        edge();
        Debug.e();
    }

    static void edge() {
        String file = "Cluster.jpg";
        Mat src0 = CVCluster.helper.loadDebugFile(file);
        Imgproc.cvtColor(src0, src0, Imgproc.COLOR_BGR2GRAY);

        Mat dst0 = new Mat();
        Imgproc.morphologyEx(src0, dst0, Imgproc.MORPH_TOPHAT, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
        Imgproc.morphologyEx(dst0, dst0, Imgproc.MORPH_GRADIENT, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
//        Imgproc.threshold(dst0, dst0, 20, 255, THRESH_BINARY);
        Imgproc.threshold(dst0, dst0, 0, 255, THRESH_BINARY | THRESH_OTSU);
        // remove noiser
        Imgproc.medianBlur(dst0, dst0, 3);
//        Imgproc.morphologyEx(dst0, dst0, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));

        Mat dst1 = new Mat();
        Imgproc.morphologyEx(src0, dst1, Imgproc.MORPH_BLACKHAT, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
        Imgproc.morphologyEx(dst1, dst1, Imgproc.MORPH_GRADIENT, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
        Imgproc.medianBlur(dst1, dst1, 3);
        Imgproc.threshold(dst1, dst1, 0, 255, THRESH_BINARY | THRESH_OTSU);
        Core.bitwise_or(dst0, dst1, dst1);
        Imgproc.morphologyEx(dst1, dst1, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
        Imgproc.dilate(dst1, dst1, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 5)));
        Debug.imshow("Origin Improve", dst0, dst1);
    }
}
