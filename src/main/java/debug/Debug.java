package debug;

import cvImgUtil.SysAsset;
import imgUtil.AbstractUtils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.HighGui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenqiu on 10/31/18.
 */
public final class Debug extends AbstractUtils{

    final static String DEBUG_DIR = "debug/";

    /**
     * debug code running time
     */
    private static long timeStart;
    private static long timeEnd;
    private static int timerId = 0;
    private static Map<Integer, Long> pairTimer;

    /**
     * @param path 图像文件名 : xxx.jpg
     * @param image
     * @throws IOException
     */
    @Override
    public void writeImage(String path, BufferedImage image) throws IOException {
        File f = newPropertyDebugFile(path);
        super.writeImage(f.getAbsolutePath(), image);
    }

    /**
     * output clustered images file
     * @param ims
     * @param templateName means xxx, such as: <p>xxx00.type</p> <p>xxx01.type</p>
     */
    public void debugImages(List<BufferedImage> ims, String templateName){
        for (int i = 0; i < ims.size(); i++){
            String name = "0" + i;
            name = name.substring(name.length() - 2, name.length());
            try {
                writeImage(templateName + name + ".jpg", ims.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void log(Object o){
        System.out.print("Debug log: ");
        if (o instanceof double[]) {
            System.out.println(Arrays.toString((double[]) o));
        } else if (o instanceof int[]){
            System.out.println(Arrays.toString((int []) o));
        } else if (o instanceof byte[]){
            System.out.println(Arrays.toString((byte []) o));
        } else {
            System.out.println(o.toString());
        }
    }

    static void reset(){
        timeStart = timeEnd = System.currentTimeMillis();
    }

    /**
     * timer start
     */
    public static void s(){
        if (pairTimer == null){
            pairTimer = new HashMap<>();
        }
        reset();
        System.out.println("code " + timerId + " running...");
        pairTimer.put(timerId++, timeStart);
    }

    /**
     * timer end
     */
    public static void e() throws Exception {
        --timerId;
        Long s = pairTimer.get(timerId);
        if (pairTimer == null || s == null){
            throw new Exception("you did not get function s() to start the timer.");
        }
        Long interval = System.currentTimeMillis() - s;
        pairTimer.remove(timerId);
        System.out.println("code " + timerId + " ending, costs " + interval / 1000.0 + "s");
    }

    /**
     * creating win to display Mats
     * @param title
     * @param m
     */
    public static void imshow(String title, Mat... m) {
        String s[] = title.split(" ");
        for (int i = 0; i < m.length; i++) {
            HighGui.imshow(s[i], m[i]);
        }
        HighGui.waitKey();
    }
}
