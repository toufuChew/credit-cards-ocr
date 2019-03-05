package imgPreProc;

import imgUtil.AbstractUtils;
import openHelper.CVLibs;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by chenqiu on 10/27/18.
 */
public class GrayTransfer extends AbstractUtils {

    /**
     * RGB 转化为灰度图
     * @param img Property 目录中图像文件名，如 "xxx.jpg"
     */
    public BufferedImage grayTransfer(String img, boolean output){
        BufferedImage OriImg = readImageByProperty(img);
        BufferedImage grayImg = new BufferedImage(OriImg.getWidth(), OriImg.getHeight(), OriImg.getType());
        final int alpha = 255;
        for (int i = 0; i < OriImg.getWidth(); i++){
            for (int j = 0; j < OriImg.getHeight(); j++){
                final int color = OriImg.getRGB(i, j);
                final int R = getRed(color);
                final int G = getGreen(color);
                final int B = getBlack(color);
                int gray = toGrayPixel(R, G, B);
                int newPixel = threeChannelPixel(alpha, gray, gray, gray);
                grayImg.setRGB(i, j, newPixel);
            }
        }
        if (output)
            writeImage("OK.jpg", grayImg);
        return grayImg;
    }

    /**
     * 将文件写入 Property 目录
     * @param path 图像文件路径
     * @param image
     */
    @Override
    public void writeImage(String path, BufferedImage image) {
        File f = newPropertyFile(path);
        try {
            String type = path.split("\\.")[1];
            ImageIO.write(image, type, f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
