package data.trained;

import cv.imgutils.AbstractCVUtils;
import cv.imgutils.CardFonts;
import cv.override.CVCluster;
import cv.override.CVDilate;
import cv.override.CVGrayTransfer;
import cv.override.CVRegion;
import debug.Debug;
import imgPreProc.GrayTransfer;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * Created by chenqiu on 3/11/19.
 */
public class DataSet {

    public static final float aspectRation = 1.579f;

    public static final int standardWidth = 28;

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
        if (f.isDirectory()) {
            num = f.listFiles().length;
        }
        long footprint = num;
        for (Mat m : set) {
            String path = f.getPath() + "/" + num + ".tif";
            Imgcodecs.imwrite(path, m);
            ++num;
        }
        System.err.println("Image data has been created to train from " + footprint + " - " + (num-1) + ".tif!");
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

    public static Rect findMainRect(Producer producer, String fileName) {
        boolean findBright = false;
        Mat gray = producer.grayMat;
        Rect bestRect = new Rect();
        final float fullWidth = gray.cols() - Producer.border * 2;
        boolean chose;
        for ( ; ; findBright = true) {
            Mat dilate = CVDilate.fastDilate(gray, findBright);
//            Debug.imshow(fileName + "[gray]", gray);
            Debug.imshow(fileName, dilate);
            Rect idRect = null;
            chose = false;
            try {
//                Mat temp = CVGrayTransfer.resizeMat(fileName, false);
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
                Debug.imshow("idRect", new Mat(gray, idRect));
            }
            if (findBright) break;
        }
        if (bestRect.width == 0) {
            System.err.println("OCR Failed.");
            exit(1);
        }
        Debug.imshow("best", new Mat(gray, bestRect));
        return bestRect;
    }

    public static void main(String []args) {
        String files[] = {
                "1555036805127.jpg",
                "Debit.jpg",
                "Credit.jpg",
                "C.jpg",
                "L.jpg",
                "A2.jpg",
                "B.jpg",
                "B2.jpg",
                "G.jpg",
                "A.jpg",
                "B.jpg",
                "E.jpg",
                "O.jpg",
                "F.jpg",
                "Credit3.jpg",
                "crop.jpg",
                "P.jpg",
        };
        for (String fileName : files) {
            Debug.s();
            Mat gray = CVGrayTransfer.grayTransferBeforeScale(fileName, false);
            Debug.log("gray.width = " + gray.cols() + ", gray.height = " + gray.rows());
            Producer producer = new Producer(gray);
            Rect mainRect = findMainRect(producer, fileName);
            producer.setRectOfDigitRow(mainRect);
            List<Mat> normalizedImg = null;
            try {
                producer.digitSeparate();
                normalizedImg = resizeDataSetImg(producer.getMatListOfDigit());
                Mat dst = new Mat();
                Core.vconcat(normalizedImg, dst);
                Debug.imshow("concat", dst);
                /**
                 * debug
                Mat dst = new Mat();
                Core.vconcat(normalizedImg, dst);
                Debug.imshow("concat", dst);
                 **/
            } catch (Exception e) {
                e.printStackTrace();
            }
//            writeDataSetImg(normalizedImg); // write out data to training
            try {
                Debug.e();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void trainedBox() {
        File f = Debug.newPropertyFile("tessBox/card.font.exp0.box");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String txt;
            String out = "";
            while ((txt = in.readLine()) != null) {
                int no = Integer.valueOf(txt.substring(txt.lastIndexOf(' ') + 1, txt.length()));
                if (no >= 39)
                    no --;
                txt = txt.substring(0, txt.lastIndexOf(' ') + 1) + no + "\n";
                out += txt;
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
            bw.write(out);
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        exit(1);
    }

}
