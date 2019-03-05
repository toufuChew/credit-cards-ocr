package imgPreProc;

import imgUtil.ImgUtils;
import imgUtil.MathUtils;
import smile.stat.distribution.KernelDensity;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by chenqiu on 10/29/18.
 */
public class GrayCluster {

    private static ClusterUtils utils;

    static {
        utils = new ClusterUtils();
    }

    final static class ClusterUtils implements MathUtils, ImgUtils{

        static final double h = 0.2;

        public final double gaussianDistribution(double x){
            double exp = Math.pow(Math.E, -0.5 * x * x);

            return 1 / Math.sqrt(2 * Math.PI) * exp;
        }

        /**
         * 找出曲线极大极小值
         * 初始化 maxIndex
         * @param points 曲线各点
         * @return
         */
        @Override
        public List<Object> lineExtreme(double[] points) {
            // true 表示递增
            boolean state = true;
            int[] maxPoints = new int[256];
            int[] minPoints = new int[256];

            int maxCur = -1;
            int minCur = -1;
            double pre = points[0];

            double maxVal = 0;

            for (int i = 1; i < points.length; pre = points[i++]){
                double cp = points[i];
                // 极大值点
                if (pre > cp && state){
                    state = false;
                    maxPoints[++maxCur] = i - 1;
                    if (pre > maxVal){
                        maxVal = pre;
                        maxIndex = i - 1;
                    }
                }
                // 极小值点
                if (!state && pre < cp) {
                    state = true;
                    minPoints[++minCur] = i - 1;
                }
            }

            List<Object> rt = new ArrayList<>();
            rt.add(Arrays.copyOf(maxPoints, maxCur + 1));
            rt.add(Arrays.copyOf(minPoints, minCur + 1));


            return rt;
        }

        @Override
        public int findMaxValueIndex(double[] values, int start, int end) {
            double maxValue = 0;
            int maxIndex = -1;
            for (int i = start; i <= end; i++){
                if (maxValue < values[i]){
                    maxValue = values[i];
                    maxIndex = i;
                }
            }
            return maxIndex;
        }

        @Override
        public int findMinValueIndex(double[] values, int start, int end) {
            double minValue = Double.MAX_VALUE;
            int minIndex = -1;
            for (int i = start; i <= end; i++){
                if (minValue > values[i]){
                    minIndex = i;
                    minValue = values[i];
                }
            }
            return minIndex;
        }

        public final double kde(int x, int []xi, int n){
            double sigma = 0;

            for (int i = 0; i < n; i++){
                double arg = (x - xi[i]) / h;
                sigma += gaussianDistribution(arg);
            }
            return 1 / (n * h) * sigma;
        }

        @Override
        public int getGray(int colorVal) throws Exception {
            if (isGray(colorVal))
                return getRed(colorVal);
            throw new Exception("不是灰度图！");
        }

        @Override
        public boolean isGray(int colorVal) {

            int red;
            return ((red = getRed(colorVal)) == getGreen(colorVal)) && (red == getBlack(colorVal));
        }


        final int bg = 110;
        final int fg = 33;
        final int overBright = 210; // 最大灰度值在 210 以上认为是光照偏亮

        // 密度估计后最大的灰度值
        // initial -1
        protected int maxIndex = -1;

        public int getMaxIndex() throws Exception {
            if (maxIndex == -1)
                throw new Exception("self definition: maxIndex initialized failed.");
            return maxIndex;
        }
    }

    /**
     * 直方图平滑处理
     * @param pixels
     * @return 平滑曲线
     */
    public double[] smooth(int[] pixels){

        double[] curve = new double[256];
        //Debug
        System.out.println("pict pixels data: " + pixels.length);

        double[] dp = Arrays.stream(pixels).asDoubleStream().toArray();
        KernelDensity kernelDensity = new KernelDensity(dp);
        for (int i = 0; i < curve.length; i++){
//            curve[i] = utils.kde(i, pixels, n);
            curve[i] = kernelDensity.p(i);
//            System.out.println(i);
        }

        return curve;
    }

    /**
     * 聚类分层
     * @param src 灰度图
     * @return List: 分层后的灰度图
     */
    public List<BufferedImage> cluster(BufferedImage src){
        int[] pixels = utils.pixelsVal(src);

        //平滑
        double[] curve = smooth(pixels);
        System.out.println(Arrays.toString(curve));
        //极值
        List plist = utils.lineExtreme(curve);
        //极小值
        int[] minGrays = (int[]) plist.get(1);

///////////////////////////////////////////////////////////////////////////
        int[] maxGrays = (int[]) plist.get(0);

        minGrays[0] = properThreshold(curve, minGrays, maxGrays);

        boolean reverse = false;
        if (minGrays[0] > utils.bg)
            reverse = true;
///////////////////////////////////////////////////////////////////////////

        //Debug
        System.out.println(Arrays.toString(minGrays));

        List<BufferedImage> ims = new ArrayList<>(minGrays.length);

        for (int i = 0; i < minGrays.length; i++) {
            BufferedImage im = Binarized.binaryImg(src, minGrays[i], reverse);
            ims.add(im);
        }
        return ims;
    }



    private int properThreshold(double[] curve, int[] minGrays, int[] maxGrays){

        int maxIndex = -1;
        try {
            maxIndex = utils.getMaxIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int bg = utils.bg;
        int fg = utils.fg;
        int overBright = utils.overBright;

        int clusterThreshold = 0;
        int mark;
        double[] localValuesMin = new double[minGrays.length];
        for (int i = 0; i < localValuesMin.length; i++){
            localValuesMin[i] = curve[minGrays[i]];
        }
        if (maxIndex < bg)
            mark = 125;
        else {
            mark = maxIndex > overBright ? 75 : 50; // 光照偏亮则前景标杆值增大
        }
        double[] score = new double[localValuesMin.length]; // abs(score i) 越小越好
        final double v1 = 1;
        for (int i = 0; i < score.length; i++){
            int symbol = -1;
            if (minGrays[i] - fg < 0) // 小于前景最低灰度（预估），abs(score i) 成倍增大
                symbol <<= ((fg - minGrays[i]) >> 2);
            if (minGrays[i] - bg > 0)
                symbol = 1;
            score[i] = symbol * Math.abs(minGrays[i] - mark) / (curve[maxIndex] - v1 * localValuesMin[i]);
            System.out.println(score[i]);
        }
        int positiveIndex = -1, negativeIndex = -1;
        for (int i = 0; i < score.length; i++){
            if ((positiveIndex == - 1 || score[positiveIndex] > score[i]) && score[i] > 0)
                positiveIndex = i;
            if ((negativeIndex == - 1 || score[negativeIndex] < score[i]) && score[i] < 0)
                negativeIndex = i;
        }
        System.out.println(maxIndex);
        clusterThreshold = maxIndex < bg ? minGrays[positiveIndex] : minGrays[negativeIndex];

        //debug
        System.out.println("clusterThreshold: " + clusterThreshold);

        return clusterThreshold;
    }



    @Deprecated
    public static int thres(BufferedImage src){
        int ptr;
        int[] srcData = utils.pixelsVal(src);
        int histData[] = new int[256];
        // Clear histogram data
        // Set all values to zero
        ptr = 0;
        while (ptr < histData.length) histData[ptr++] = 0;

        // Calculate histogram and find the level with the max value
        // Note: the max level value isn't required by the Otsu method
        ptr = 0;
        int maxLevelValue = 0;
        while (ptr < srcData.length)
        {
            int h = 0xFF & srcData[ptr];
            histData[h] ++;
            if (histData[h] > maxLevelValue) maxLevelValue = histData[h];
            ptr ++;
        }

        // Total number of pixels
        int total = srcData.length;

        float sum = 0;
        for (int t=0 ; t<256 ; t++) sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for (int t=0 ; t<256 ; t++)
        {
            wB += histData[t];					// Weight Background
            if (wB == 0) continue;

            wF = total - wB;						// Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB;				// Mean Background
            float mF = (sum - sumB) / wF;		// Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        threshold -= 50;
        System.err.println("threshold: " + threshold);
        return threshold;
    }

}
