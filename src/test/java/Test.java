import imgPreProc.Binarized;
import imgPreProc.GrayTransfer;
import imgUtil.AbstractUtils;
import org.bytedeco.javacpp.lept;
import static org.bytedeco.javacpp.lept.*;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.bytedeco.javacpp.lept.pixDestroy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenqiu on 11/16/18.
 */
public class Test {

    static AbstractUtils utils = new TestUtils();

    final static class TestUtils extends AbstractUtils{}

    public static BufferedImage WellneradaptiveThreshold1(BufferedImage src){
        int width = src.getWidth();
        int height = src.getHeight();
        final int radius = width / 8;
        final int threshold = 15;
        int intervalThreshold = 100 - threshold;
        BufferedImage out = new BufferedImage(width, height, src.getType());

        int[] row = new int[width];
        int sum;
        for (int y = 0; y < height; y++){
            int x = 0;
            int grayscale = utils.getGray(src.getRGB(x, y));
            sum = grayscale * radius;
            for (; x < width; x++){
                int pi = utils.getGray(src.getRGB(x, y));
                int prepi = grayscale;
                int prex = x - radius;
                if (prex > 0) {
                    prepi = utils.getGray(src.getRGB(prex, y));
                }
                sum += pi - prepi;
                int alpha = utils.getAlpha(src.getRGB(x, y));
                if (pi * 100 * radius < sum * intervalThreshold)
                    out.setRGB(x, y, utils.threeChannelPixel(alpha, 0, 0, 0));
                else out.setRGB(x, y, utils.threeChannelPixel(alpha, 255, 255, 255));
            }
        }
        return out;
    }

    public static BufferedImage WellneradaptiveThreshold2(BufferedImage src){
        int width = src.getWidth();
        int height = src.getHeight();
        final int threshold = 50;
        int intervalThreshold = 100 - threshold;
        final int radius = 500;
        int[] integral = new int[width * height];
        int indexOne, indexTwo;
        int sum;
        for (int y = 0; y < height; y++){
            sum = 0;
            indexOne = y * width;
            for (int x = 0; x < width; x++){
                int grayscale = utils.getGray(src.getRGB(x, y));
                sum += grayscale;
                if (y == 0) {
                    integral[indexOne] = sum;
                }
                else{
                    integral[indexOne] = integral[indexOne - width] + sum;
                }
                indexOne++;
            }
        }
        BufferedImage out = new BufferedImage(width, height, src.getType());
        int x1, x2, y1, y2, y2y1;
        for (int y = 0; y < height; y++){
            y1 = y - radius;
            y2 = y + radius;
            if (y1 < 0)
                y1 = 0;
            if (y2 >= height)
                y2 = height - 1;
            indexOne = y1 * width;
            indexTwo = y2 * width;
            y2y1 = (y2 - y1) * 100;
            for (int x = 0; x < width; x++){
                x1 = x - radius;
                x2 = x + radius;
                if (x1 < 0)
                    x1 = 0;
                if (x2 >= width)
                    x2 = width - 1;
                sum = integral[indexTwo + x2] - integral[indexOne + x2] - integral[indexTwo + x1] + integral[indexOne + x1];
                final int rgb = src.getRGB(x, y);
                int pi = utils.getGray(rgb);
                int alpha = utils.getAlpha(rgb);
                if (pi * (x2 - x1) * y2y1 < sum * intervalThreshold)
                    out.setRGB(x, y, utils.threeChannelPixel(alpha, 0, 0, 0));
                else
                    out.setRGB(x, y, utils.threeChannelPixel(alpha, 255, 255, 255));
            }
        }
        return out;
    }

    public static BufferedImage filter(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage dest;
        dest = new BufferedImage(width, height, src.getType());

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++) {
                inPixels[i * width + j] = src.getRGB(j, i);
            }
        }
        int index = 0;
        int means = getThreshold(inPixels, height, width);
        for(int row=0; row<height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for(int col=0; col<width; col++) {
                index = row * width + col;
                ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
                if(tr > means) {
                    tr = tg = tb = 255; //white
                } else {
                    tr = tg = tb = 0; // black
                }
                outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
            }
        }
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++)
                dest.setRGB(j, i, outPixels[i * width + j]);
        }
        return dest;
    }

    private static int getThreshold(int[] inPixels, int height, int width) {
        // maybe this value can reduce the calculation consume;
        int inithreshold = 127;
        int finalthreshold = 0;
        int temp[] = new int[inPixels.length];
        for(int index=0; index<inPixels.length; index++) {
            temp[index] = (inPixels[index] >> 16) & 0xff;
        }
        List<Integer> sub1 = new ArrayList<Integer>();
        List<Integer> sub2 = new ArrayList<Integer>();
        int means1 = 0, means2 = 0;
        while(finalthreshold != inithreshold) {
            finalthreshold = inithreshold;
            for(int i=0; i<temp.length; i++) {
                if(temp[i] <= inithreshold) {
                    sub1.add(temp[i]);
                } else {
                    sub2.add(temp[i]);
                }
//                System.out.println(temp[i]);
            }
            means1 = getMeans(sub1);
            means2 = getMeans(sub2);
            sub1.clear();
            sub2.clear();
            inithreshold = (means1 + means2) / 2;
        }
        long start = System.currentTimeMillis();
        System.out.println("Final threshold  = " + finalthreshold);
        long endTime = System.currentTimeMillis() - start;
        System.out.println("Time consumes : " + endTime);
        return finalthreshold;
    }

    private static int getMeans(List<Integer> data) {
        int result = 0;
        int size = data.size();
        for(Integer i : data) {
            result += i;
        }
        return (result/size);
    }

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
