package imgUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by chenqiu on 10/27/18.
 */
public interface ImgUtils extends ImgColors {

    default BufferedImage readImageByProperty(String fileName){
        return null;
    }

    default BufferedImage readImageByLocal(String localPath){
        return null;
    }

    /**
     * 图像文件写入目录
     * @param path 图像文件路径
     * @param image
     */
    default void writeImage(String path, BufferedImage image) throws IOException {
        File f = new File(path);
        ImageIO.write(image, path.split("\\.")[1], f);
    }

    default int[] pixelsVal(BufferedImage image){

        int[] pixels = new int[image.getHeight() * image.getWidth()];
        int cur = 0;
        for (int i = 0; i < image.getWidth(); i++){
            for (int j = 0; j < image.getHeight(); j++){
                try {
                    pixels[cur++] = getGray(image.getRGB(i, j));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return pixels;
    }

    /**
     * 反转黑白色
     * please use binaryImg(BufferedImage , int , boolean)
     * @param image
     * @return
     */
    default BufferedImage reverseBW(BufferedImage image){
        try {
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    final int rgb = image.getRGB(i, j);
                    final int gray = getGray(rgb);
                    final int reverse = (~gray) & 0xff;
                    int newGray = threeChannelPixel(getAlpha(rgb), reverse, reverse, reverse);
                    image.setRGB(i, j, newGray);
                }
            }
        } catch (Exception e){
            System.err.println("Image is not gray.");
        }
        return image;
    }
}
