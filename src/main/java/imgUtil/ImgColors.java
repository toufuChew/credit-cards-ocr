package imgUtil;

import java.awt.image.BufferedImage;

/**
 * Created by chenqiu on 10/29/18.
 */
public interface ImgColors {

    int WHITE = 0xff;

    int BLACK = 0;

    int colorMask = 0xff;
    /**
     * <p>构建三通道像素</p>
     * @param alpha
     * @param R
     * @param G
     * @param B
     * @return
     */
    default int threeChannelPixel(int alpha, int R, int G, int B){
        int newPixel = 0;
        R &= 0xff;
        G &= 0xff;
        B &= 0xff;
        newPixel = (newPixel + alpha) << 8;
        newPixel = (newPixel + R) << 8;
        newPixel = (newPixel + G) << 8;
        newPixel = newPixel + B;
        return newPixel;
    }

    int getGray(int colorVal) throws Exception;

    default int toGrayPixel(int R, int G, int B){
        return (int) Math.floor(0.299 * R + 0.587 * G + 0.114 * B);
    }

    boolean isGray(int colorVal);

    default int getAlpha(int coloVal){
        return coloVal >>> 24;
    }

    default int getRed(int colorVal){
        return colorVal >>> 16 & colorMask;
    }

    default int getGreen(int colorVal){
        return colorVal >>> 8 & colorMask;
    }

    default int getBlack(int colorVal){
        return colorVal & colorMask;
    }

    default boolean isBlack(int colorVal){
        try {
            return getGray(colorVal) == BLACK;
        } catch (Exception e) {
            return false;
        }
    }

    default boolean isWhite(int colorVal){
        try {
            return getGray(colorVal) == WHITE;
        } catch (Exception e) {
            return false;
        }
    }
}
