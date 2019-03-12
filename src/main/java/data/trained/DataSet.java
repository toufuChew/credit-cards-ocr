package data.trained;

import cv.imgutils.CardFonts;
import cv.imgutils.RectFilter;
import cv.imgutils.SysAsset;
import cv.override.CVCluster;
import cv.override.CVDilate;
import cv.override.CVGrayTransfer;
import cv.override.CVRegion;
import debug.Debug;
import imgToText.TessReg;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * Created by chenqiu on 3/11/19.
 */
public class DataSet {

    public static final float aspectRation = 1.579f;

    public static final int standardWidth = 280;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static List<Mat> resizeDataSetImg(List<Mat> set) {
        List<Mat> rstList = new ArrayList<>();
        for (Mat m : set) {
            rstList.add(CVGrayTransfer.resizeMat(m, standardWidth, (int)(standardWidth * aspectRation)));
        }
        return rstList;
    }

    public static void writeDataSetImg(List<Mat> set) {
        File f = Debug.newPropertyFile("../dataset/");
        long num = 0;
//        if (f.isDirectory()) {
//            num = f.listFiles().length;
//        }
        for (Mat m : set) {
            String path = f.getPath() + "/" + num + ".jpg";
            Imgcodecs.imwrite(path, m);
            ++num;
        }
    }

    static class Producer extends CVRegion {

        public Producer(Mat graySrc) {
            super(graySrc);
        }

        @Override
        protected void paintDigits(List<Integer> cuttingList) {
            Mat digits;
            if (getFontType() == CardFonts.FontType.BLACK_FONT)
                digits = getBinDigitRegion();
            else digits = new Mat(grayMat, rectOfDigitRow);
//            Debug.imshow(CardFonts.fontTypeToString(getFontType()), digits);
            Rect cutter = new Rect(0, 0, 0, rectOfDigitRow.height);
            for (int i = 1; i < cuttingList.size(); i++) {
                if ((i & 0x1) == 0)
                    continue;
                int x1 = cuttingList.get(i - 1);
                int x2 = cuttingList.get(i);
                cutter.x = x1; cutter.width = x2 - x1;
                Rect ofY = cutEdgeOfY(new Mat(getBinDigitRegion(), cutter));
                matListOfDigit.add(new Mat(digits, new Rect(x1, ofY.y, cutter.width, ofY.height)));
            }
        }
    }

    public static Rect findMainRect(Producer producer) {
        boolean findBright = false;
        Mat gray = producer.grayMat;
        Rect bestRect = new Rect();
        final float fullWidth = gray.cols() - Producer.border * 2;
        boolean chose;
        for ( ; ; findBright = true) {
            Mat dilate = CVDilate.fastDilate(gray, findBright);
            Rect idRect = null;
            chose = false;
            try {
                idRect = producer.digitRegion(dilate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (idRect != null) {
                if (bestRect.width == 0)
                    chose = true;
                else if (idRect.width < fullWidth) {
                    if (bestRect.width == fullWidth || idRect.width > bestRect.width)
                        chose = true;
                }
                if (chose) {
                    bestRect = idRect;
                }
            }
            if (findBright) break;
        }
        if (bestRect.width == 0) {
            System.err.println("OCR Failed.");
            exit(1);
        }
        return bestRect;
    }

    public static void main(String []args) {
        Debug.s();
        String fileName = "E.jpg";
        Mat gray = CVGrayTransfer.grayTransferBeforeScale(fileName, false);
        Debug.log("gray.width = " + gray.cols() + ", gray.height = " + gray.rows());
        Producer producer = new Producer(gray);
        Rect mainRect = findMainRect(producer);
        producer.setRectOfDigitRow(mainRect);
        List<Mat> normalizedImg = null;
        try {
            producer.digitSeparate();
            normalizedImg = resizeDataSetImg(producer.getMatListOfDigit());
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeDataSetImg(normalizedImg);
        try {
            Debug.e();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
