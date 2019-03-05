package imgPreProc;

import java.awt.image.BufferedImage;

/**
 * Created by chenqiu on 10/31/18.
 */
public abstract class AbstractErosion {

    protected final static char[][] element = {
            {0xff, 0xff, 0xff},
            {0xff, 0, 0xff},
            {0xff, 0xff, 0xff}
    };

    /**
     * 二值图腐蚀
     * @param src 目标图像
     */
    public BufferedImage erode(BufferedImage src){
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        for (int i = 1; i < src.getWidth() - 1; i++){
            for (int j = 1; j < src.getHeight() - 1; j++){
                fill(true, src, i, j, dst);
            }
        }
        return dst;
    }

    /**
     * 二值图膨胀
     * @param src 目标图像
     * @return
     */
    public BufferedImage dilate(BufferedImage src){
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        for (int i = 1; i < src.getWidth() - 1; i++){
            for (int j = 1; j < src.getHeight() - 1; j++){
                fill(false, src, i, j, dst);
            }
        }
        return dst;
    }

    abstract void fill(boolean isErosion, BufferedImage src, int x, int y, BufferedImage dst);
}
