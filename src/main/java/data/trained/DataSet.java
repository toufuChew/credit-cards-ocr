package data.trained;

import cv.imgutils.CardFonts;
import cv.override.CVDilate;
import cv.override.CVGrayTransfer;
import cv.override.CVRegion;
import debug.Debug;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * Created by chenqiu on 3/11/19.
 *
 * main method
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
        File f = Debug.newPropertyFile("../dataset/digits/new");
        long num = 0;
        if (f.isDirectory()) {
            num = f.listFiles().length;
        }
        long footprint = num;
        for (int i = 0; i < set.size(); i++) {
            String path = f.getPath() + "/" + num + ".tif";
            Imgcodecs.imwrite(path, set.get(i));
            ++num;
        }
        System.err.println("Image data has been created to train from " + footprint + " - " + (num-1) + ".tif!");
    }

    static class Producer extends CVRegion {

        protected Mat bgrMat;

        public Producer(Mat graySrc) {
            super(graySrc);
            bgrMat = null;
        }
        public Producer(Mat graySrc, Mat bgrMat) {
            super(graySrc);
            this.bgrMat = bgrMat;
        }

        @Override
        protected void paintDigits(List<Integer> cuttingList) {
            Mat digits;
            digits = new Mat(bgrMat == null ? grayMat : bgrMat, rectOfDigitRow);
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

        public Rect findMainRect() {
            boolean findBright = false;
            Mat gray = this.grayMat;
            Rect bestRect = new Rect();
            final float fullWidth = gray.cols() - Producer.border * 2;
            boolean chose;
            for ( ; ; findBright = true) {
                Mat dilate = CVDilate.fastDilate(gray, findBright);
//            Debug.imshow(fileName + "[gray]", gray);
//            Debug.imshow(fileName, dilate);
                Rect idRect = null;
                chose = false;
                try {
//                Mat temp = CVGrayTransfer.resizeMat(fileName, false);
                    idRect = this.digitRegion(dilate);
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
//                Debug.imshow("idRect", new Mat(gray, idRect));
                }
                if (findBright) break;
            }
            if (bestRect.width == 0) {
                System.err.println("OCR Failed.");
                exit(1);
            }
//        Debug.imshow("best", new Mat(gray, bestRect));
            return bestRect;
        }
    }


    public static void main(String []args) {
        String files[] = {
                "20190427173208.png",
                "1556329963986.jpg",
                "1556330116077.jpg",
                "1556330682287.jpg",
                "B2.jpg",
                "A.jpg",
                "B.jpg",
                "Credit.jpg",
                "A.jpg",
                "A2.jpg",
                "Credit3.jpg",
                "B.jpg",
                "L.jpg",
                "C.jpg",
                "Debit.jpg",
                "G.jpg",
                "E.jpg",
                "O.jpg",
                "F.jpg",
                "crop.jpg",
                "P.jpg",
        };
        for (String fileName : files) {
            Debug.s();
            Mat gray = CVGrayTransfer.grayTransferBeforeScale(fileName, false);
            Debug.log("gray.width = " + gray.cols() + ", gray.height = " + gray.rows());
            Producer producer = new Producer(gray, CVGrayTransfer.resizeMat(fileName, false));
            Rect mainRect = producer.findMainRect();
            producer.setRectOfDigitRow(mainRect);
            List<Mat> normalizedImg = null;
            try {
                producer.digitSeparate();
                Mat dst = producer.getMatListOfDigit().get(0).clone();
                if (producer.getFontType() == CardFonts.FontType.LIGHT_FONT) {
                    normalizedImg = resizeDataSetImg(producer.getMatListOfDigit());
                    Core.vconcat(normalizedImg, dst);
                }
//                Debug.imshow("concat", dst);
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

    public static void classify(Mat ipt) {
        File[] digits = Debug.newPropertyFile("../dataset/digits").listFiles();
        double maxScore = 0;
        String result = null;
        for (File digit : digits) {
            String path = digit.getAbsolutePath();
            Mat digitMat = Imgcodecs.imread(path);
            digitMat = CVGrayTransfer.grayTransfer(digitMat);
            Mat outMat = new Mat();
            Imgproc.matchTemplate(ipt, digitMat, outMat, Imgproc.TM_CCOEFF);
            Core.MinMaxLocResult mmlResult = Core.minMaxLoc(outMat);
            if (maxScore < mmlResult.maxVal) {
                result = path.substring(path.indexOf(".tif") - 1).substring(0, 1);
                maxScore = mmlResult.maxVal;
            }
        }
        System.out.println(result);
        Debug.imshow("score:" + maxScore, ipt);

    }

}
