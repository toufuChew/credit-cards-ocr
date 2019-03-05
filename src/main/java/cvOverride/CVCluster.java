package cvOverride;

import cvImgUtil.AbstractCVUtils;
import debug.Debug;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import smile.stat.distribution.KernelDensity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chenqiu on 11/26/18.
 */
public class CVCluster {

    public static final int GRAYSCALE = 256;
    protected static MathUtil helper = new MathUtil();
    /**
     * detect result of card id-font type
     * the default is black
     */
    public ClusterType type = ClusterType.BLACK_FONT;
    public enum ClusterType {
        BLACK_FONT,
        LIGHT_FONT
    }
    static final class MathUtil extends AbstractCVUtils {

        public static int indexOfMaxValue(double[] array){
            double max = Double.MIN_VALUE;
            int index = -1;
            for (int i = 0; i < array.length; i++){
                if (max < array[i]){
                    index = i;
                    max = array[i];
                }
            }
            return index;
        }

        @Override
        public void writeFile(File f, String absolutePath) {
        }

        public double[] pixelsScale(Mat mat){
            mat = CVGrayTransfer.resizeMat(mat, 512, false);
            int row = mat.rows();
            int col = mat.cols();
            double[] pixes = new double[row * col];
            int cur = 0;
            int cols = 0;
            int cole = col - cols;
            for (int i = 0; i < row; i++){
                for (int j = cols; j < cole; j++){
                    pixes[cur++] = mat.get(i, j)[0];
                }
            }
            return pixes;
        }

        double[] kernelDensity(double[] flow){
            double[] curve = new double[GRAYSCALE];
            KernelDensity kernelDensity = new KernelDensity(flow);
            for (int x = 0; x < curve.length; x++){
                curve[x] = kernelDensity.p(x);
            }
            return curve;
        }

        Object[] pointsExt(double[] points){
            // true 表示递增
            boolean state = true;
            int[] maxPoints = new int[GRAYSCALE];
            int[] minPoints = new int[GRAYSCALE];
            double[] maxVal = new double[GRAYSCALE];
            double[] minVal = new double[GRAYSCALE];

            int maxCur = -1;
            int minCur = -1;
            double pre = points[0];


            for (int i = 1; i < points.length; pre = points[i++]){
                double cp = points[i];
                // 极大值点
                if (pre > cp && state){
                    state = false;
                    maxPoints[++maxCur] = i - 1;
                    maxVal[maxCur] = pre;
                }
                // 极小值点
                if (!state && pre < cp) {
                    state = true;
                    minPoints[++minCur] = i - 1;
                    minVal[minCur] = pre;
                }
            }

            return new Object[]{
                    Arrays.copyOf(maxPoints, maxCur + 1),
                    Arrays.copyOf(maxVal, maxCur + 1),
                    Arrays.copyOf(minPoints, minCur + 1),
                    Arrays.copyOf(minVal, minCur + 1)
            };
        }
        final int TOPGRAYS = 0;
        final int LOWGRAYS = 2;
        final int TOPVALUES = 1;
        final int LOWVALUES = 3;
    }


    /**
     * 灰度聚类
     * <p>note that: 如果图片过暗可能导致失败</p>
     * @param src 灰度图
     * @return 聚类后二值图
     */
    public Mat cluster(Mat src){

        double []pixels = helper.pixelsScale(src);

        double []curve = helper.kernelDensity(pixels);
        // debug
        Debug.log(curve);

        int reverse = Imgproc.THRESH_BINARY_INV;
        int threshold = properThreshold(curve);
        if (threshold > BACKGROUND){
            reverse = Imgproc.THRESH_BINARY;
            // top-hat enhance contrast
            Imgproc.morphologyEx(src, src, Imgproc.MORPH_TOPHAT,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 3)));
            this.type = ClusterType.LIGHT_FONT;
        }
        final int white = 0xff;
        Mat dst = new Mat();
        Imgproc.threshold(src, dst, threshold, white, reverse);
        return dst;
    }

    protected static final int BACKGROUND = 110;
    protected static final int FOREGROUND = 33;
    protected static final int OVERBRIGHT = 210;

    private static int properThreshold(double[] kdeCurve){
        Object[] plist = helper.pointsExt(kdeCurve);
        int threshold = -1;
        int maxIndex = helper.indexOfMaxValue((double[]) plist[helper.TOPVALUES]);
        maxIndex = ((int[]) plist[helper.TOPGRAYS])[maxIndex];
        int mask;

        if (maxIndex < BACKGROUND){
            mask = 125;
        }
        else {
            mask = maxIndex > OVERBRIGHT ? 75 : 50;
        }
        double[] minValues = (double[]) plist[helper.LOWVALUES];
        int[] minGrays = (int[]) plist[helper.LOWGRAYS];
        double[] scores = new double[minValues.length];
        final double v1 = 1;

        for (int i = 0; i < scores.length; i++){
            int symbol = -1;
            if (minGrays[i] - FOREGROUND < 0)
                symbol <<= ((FOREGROUND - minGrays[i]) >> 2);
            if (minGrays[i] - BACKGROUND > 0)
                symbol = 1;
            scores[i] = symbol * Math.abs(minGrays[i] - mask) / (kdeCurve[maxIndex] - v1 * minValues[i]);
        }
        // debug
        Debug.log(scores);
        Debug.log(minGrays);
        Debug.log(maxIndex);

        int positiveIndex = -1, negativeIndex = -1;
        for (int i = 0; i < scores.length; i++){
            if ((positiveIndex == - 1 || scores[positiveIndex] > scores[i]) && scores[i] > 0)
                positiveIndex = i;
            if ((negativeIndex == - 1 || scores[negativeIndex] < scores[i]) && scores[i] < 0)
                negativeIndex = i;
        }
        threshold = maxIndex < BACKGROUND ? minGrays[positiveIndex] : minGrays[negativeIndex];

        Debug.log("cluster threshold: " + threshold);
        return threshold;
    }

}
