package cv.override;

import cv.imgutils.CardFonts;
import debug.Debug;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chenqiu on 3/7/19.
 */
public class CVFontType {

    // the font area ratio
    private static final float ratio = 0.1f;
    // segments numb of font
    private static final int fontSegments = 23;

    /**
     * recognize the font type
     * @param gray0 1-channel image cropped
     * @return the region's font type
     */
    public static CardFonts getFontType(Mat gray0) {
        int cols = gray0.cols();
        int rows = gray0.rows();
        CardFonts cardFonts = new CardFonts();
        cardFonts.setType(CardFonts.FontType.LIGHT_FONT);
//        2.0
//        Mat hsv = new Mat();
//        Imgproc.cvtColor(src0, hsv, Imgproc.COLOR_BGR2HSV);
//        Mat bin = new Mat();
//        Core.inRange(hsv, new Scalar(0, 0, 0), new Scalar(180, 255, 46), bin);
//        Imgproc.threshold(bin, bin, 1, 255, Imgproc.THRESH_BINARY);
//        Debug.imshow("", bin);

//        3.0
        Debug.s();
        final int thresh = 60;
        Mat bin = new Mat();
        Imgproc.threshold(gray0, bin, thresh, 255, Imgproc.THRESH_BINARY_INV);
        Imgproc.medianBlur(bin, bin, 3);
        cardFonts.setFonts(bin);
        int whiteBits = bitwise_sum(bin);
//        Debug.imshow("", bin);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(bin, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        if (contours.size() == 0) {
            return cardFonts;
        }
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                return -(int)(Imgproc.contourArea(o1) - Imgproc.contourArea(o2));
            }
        });
        double maxArea = Imgproc.contourArea(contours.get(0));
        double minArea = Imgproc.contourArea(contours.get(contours.size() - 1));
        //origin almost black
        if (whiteBits < cols * rows * ratio)
            return cardFonts;
        // too many small debris, it will affect separating character later
        if (contours.size() > fontSegments)
            return cardFonts;
        if (maxArea * ratio > minArea || maxArea == minArea) { // almost white (> 80%) or contains big white area
            Mat not = new Mat();
            Core.bitwise_not(bin, not);
            contours = new ArrayList<>();
            Imgproc.findContours(not, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            Collections.sort(contours, new Comparator<MatOfPoint>() {
                @Override
                public int compare(MatOfPoint o1, MatOfPoint o2) {
                    return -(int)(Imgproc.contourArea(o1) - Imgproc.contourArea(o2));
                }
            });
            whiteBits = cols * rows - whiteBits;
            cardFonts.setFonts(not);
//            Debug.imshow("not", not);
        }
        final int checkTimes = 5;
        // check whether contains large amounts of debris
        if (contours.size() <= checkTimes * 2 || contours.size() > fontSegments) {
            return cardFonts;
        }
        maxArea = Imgproc.contourArea(contours.get(checkTimes));
        double normal = Imgproc.contourArea(contours.get(contours.size() - checkTimes));
        if (maxArea * ratio * checkTimes > normal * 2) { // 0.25 * digit8 = digit1
            return cardFonts;
        }
        try {
            Debug.e();
        } catch (Exception e) {
            e.printStackTrace();
        }
        cardFonts.setType(CardFonts.FontType.BLACK_FONT);
//        Debug.imshow(fontTypeToString(type), bin);
        return cardFonts;
    }

    private static int bitwise_sum(Mat bin0) {
        int cols = bin0.cols();
        int rows = bin0.rows();
        byte buff[] = new byte[cols * rows];
        int sum = 0;
        bin0.get(0, 0, buff);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++)
                if (buff[i * cols + j] != 0)
                    sum++;
        }
        return sum;
    }

}
