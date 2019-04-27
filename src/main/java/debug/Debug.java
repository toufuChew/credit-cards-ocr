package debug;

import cv.imgutils.AbstractCVUtils;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by chenqiu on 10/31/18.
 */
public final class Debug extends AbstractCVUtils {

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
    public void writeImage(String path, BufferedImage image) throws IOException {
        File f = newPropertyDebugFile(path);
        ImageIO.write(image, path.split("\\.")[1], f);
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

    @Override
    public void writeFile(File f, String absolutePath) {
    }


    @Override
    public Mat pickRectROI(Mat mat0, int n) {
        Mat color = grayToBGR(mat0);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mat0, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                return -((int) (Imgproc.contourArea(o1) - Imgproc.contourArea(o2))); // decrease
            }
        });
        int count = n > 0 ? n : contours.size();
        for (MatOfPoint cnt : contours) {
            if (count-- == 0) break;
            Rect r = Imgproc.boundingRect(cnt);
            Imgproc.rectangle(color, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 0, 255), 2);
        }
        return color;
    }

    /**
     * paint rect of contours
     * @param title
     * @param mat0
     * @param pickContours number of contour to display
     */
    public static void pickRectROI(String title, Mat mat0, int pickContours) {
        Debug debug = new Debug();
        imshow(title, debug.pickRectROI(mat0, pickContours));
    }
}
