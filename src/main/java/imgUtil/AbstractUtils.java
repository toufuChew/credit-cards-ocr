package imgUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenqiu on 10/27/18.
 */
public abstract class AbstractUtils implements ImgUtils {

    protected static final String DIR_PROPERTY = "user.dir";

    protected static final String RELATIVE_DIR = "/res/img/origin/";

    protected static final String DEBUG_DIR = "/res/img/debug/";

    /**
     * <tt>for <strong>origin</strong> image file</tt>
     * @param fileName
     * @return
     */
    public static File newPropertyFile(String fileName){
        return new File(System.getProperty(DIR_PROPERTY) + RELATIVE_DIR + fileName);
    }

    /**
     * <tt>for <strong>debug</strong> image file</tt>
     * @param fileName
     * @return
     */
    public static File newPropertyDebugFile(String fileName){
        return new File(System.getProperty(DIR_PROPERTY) + DEBUG_DIR + fileName);
    }

    /**
     * read <strong>origin</strong> image file
     * @param fileName xxx.type
     * @return
     */
    @Override
    public BufferedImage readImageByProperty(String fileName) {
        File f = newPropertyFile(fileName);
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <p>read <strong>absolute</strong> path image file</p>
     * @param localPath absolute path
     * @return
     */
    @Override
    public BufferedImage readImageByLocal(String localPath) {
        File f = new File(localPath);
        if (!f.exists()){
            System.err.println("self definition:  file not exits " + localPath);
            return null;
        }
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <p><em>unsafe</em></p>
     * 确保是灰度图的情况下使用
     * @param colorVal
     * @return
     */
    @Override
    public int getGray(int colorVal) {
        return getRed(colorVal);
    }

    public boolean isGray(int colorVal){
        int red;
        return ((red = getRed(colorVal)) == getGreen(colorVal)) && (red == getBlack(colorVal));
    }


}
