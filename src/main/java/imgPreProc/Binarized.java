package imgPreProc;

import imgUtil.ImgColors;

import java.awt.image.BufferedImage;

/**
 * Created by chenqiu on 10/28/18.
 */
public class Binarized extends AbstractErosion implements ImgColors {

    public static Binarized binarized;

    static {
        binarized = new Binarized();
    }
    /**
     * 实现二值化:
     * <blockquote><pre>
     * dst(x, y) = {
     *     maxval   if src(x,y) > thresh
     *     0        otherwise
     * }
     * </pre></blockquote>
     * @param src
     * @param thresh
     * @param maxval
     * @return
     */
    public BufferedImage threshold(BufferedImage src, double thresh, double maxval){
        BufferedImage im = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        for (int i = 0; i < src.getWidth(); i++){
            for (int j = 0; j < src.getHeight(); j++){
                final int color = src.getRGB(i, j);
                final int alpha = getAlpha(color);
                int dstVal = getGray(color);
                if (dstVal > thresh)
                    dstVal = (int) maxval;
                else dstVal = 255 - (int)maxval;
                int dstPixel = threeChannelPixel(alpha, dstVal, dstVal, dstVal);
                im.setRGB(i, j, dstPixel);
            }
        }
        return im;
    }

    public static BufferedImage binaryImg(BufferedImage src, double thresh, boolean reverse){
        int maxVal = reverse ? 0 : 255;
        return binarized.threshold(src, thresh, maxVal);
    }

    @Override
    public int getGray(int colorVal) {

        return isGray(colorVal) ? getRed(colorVal) : -1;
    }

    @Override
    public boolean isGray(int colorVal) {
        int red;
        return ((red = getRed(colorVal)) == getGreen(colorVal)) && (red == getBlack(colorVal));
    }

    @Override
    void fill(boolean isErosion, BufferedImage src, int x, int y, BufferedImage dst) {
        int c;
        final int central = src.getRGB(x, y);
        final int ch = isErosion ? BLACK : WHITE;

        for (int i = x - 1; i <= x + 1; i++){
            for (int j = y - 1; j <= y + 1; j++){
                if (i == x && j == y)
                    continue;
                final int color = src.getRGB(i, j);
                int binary = getGray(color);
                /**
                 * 1. 出现黑色且为腐蚀 ,@reference
                 * http://www.opencv.org.cn/opencvdoc/2.3.2/html/doc/tutorials/imgproc/erosion_dilatation/erosion_dilatation.html#id5
                 * 2. 出现白色且为膨胀 ,@reference
                 * http://www.opencv.org.cn/opencvdoc/2.3.2/html/doc/tutorials/imgproc/erosion_dilatation/erosion_dilatation.html#id4
                 */
                if (element[i - x + 1][j - y + 1] == 0xff
                        && (ch == binary)) {
                    c = threeChannelPixel(getAlpha(central), ch, ch, ch);
                    dst.setRGB(x, y, c);
                    return;
                }

            }
        }
        c = threeChannelPixel(getAlpha(central), ~ch, ~ch, ~ch);
        dst.setRGB(x, y, c);
    }

    /**
     * 腐蚀
     * @param src
     * @return
     */
    public static BufferedImage erosion(BufferedImage src){
        return binarized.erode(src);
    }

    /**
     * 膨胀
     * @param src
     * @return
     */
    public static BufferedImage dilation(BufferedImage src){
        return binarized.dilate(src);
    }
}
