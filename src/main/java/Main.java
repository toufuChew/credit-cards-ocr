import debug.Debug;
import imgPreProc.Binarized;
import imgPreProc.GrayCluster;
import imgPreProc.GrayTransfer;
import imgToText.TessReg;
import imgUtil.AbstractUtils;
import imgUtil.ImgUtils;
import org.bytedeco.javacpp.tesseract;
import smile.stat.distribution.KernelDensity;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by chenqiu on 10/29/18.
 *
 * for Debug
 */
public class Main {

    public static FinalAbstractUtils utils = new FinalAbstractUtils();

    public static Debug debug = new Debug();

    static final class FinalAbstractUtils extends AbstractUtils {

        /**
         * to debug
         */
        public void stop(){
            try {
                System.err.println("Test has stopped.");
                Object o = new Object();
                synchronized (o) {
                    o.wait();
                }
            } catch (InterruptedException e) {
                System.exit(1);
            }
        }
    }

    public static void main(String []args){

//        BufferedImage src = new GrayTransfer().grayTransfer("Credit.jpeg", false);
//        BufferedImage img = Binarized.binaryImg(src, GrayCluster.thres(src));
//        try {
//            utils.writeImage("/Users/chenqiu/IdeaProjects/CardIDRecognition/res/img/debug/Cluster.jpg", img);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        utils.stop();

        imgTest();

//        recognizeTest("Cluster.png");

    }

    public static void recognizeTest(String debugFile){
        File f = utils.newPropertyDebugFile(debugFile);

        String text = TessReg.getOCRText(f);

        System.out.println(text);
    }

    public static void imgTest() {
//        new GrayTransfer().grayTransfer("Credit.jpeg");

        GrayTransfer trans = new GrayTransfer();

        BufferedImage im = trans.grayTransfer("Credit.jpeg", false);

        //cluster
        GrayCluster grayCluster = new GrayCluster();

        List<BufferedImage> list = grayCluster.cluster(im);

        debug.debugImages(list, "Cluster");

        utils.stop();
        // erode
        List<BufferedImage> erodeList = new LinkedList<>();
        for (int i = 0; i < list.size(); i++){
            erodeList.add(Binarized.erosion(list.get(i)));
        }
        debug.debugImages(erodeList, "Erode");

    }

    public static BufferedImage resizeImage(BufferedImage src, int resizeWidth, GrayTransfer trans){
        final int width = src.getWidth();
        final int height = src.getHeight();
        double rate = resizeWidth / width;
        int newWidth = resizeWidth;
        int newHeigth = (int) (height * rate);
        BufferedImage resizeImg = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        Graphics2D g = resizeImg.createGraphics();
        g.drawImage(src, 0, 0, newWidth, newHeigth, null);
        g.dispose();
        trans.writeImage("debug/small.jpg", resizeImg);
        return resizeImg;
    }
}
