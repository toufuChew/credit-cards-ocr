package imgToText;

import org.bytedeco.javacpp.BytePointer;
import static org.bytedeco.javacpp.lept.*;

import org.bytedeco.javacpp.tesseract;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;
import static org.bytedeco.javacpp.lept.pixDestroy;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Created by chenqiu on 11/12/18.
 */
public class TessReg {

    static TessBaseAPI mTess;

    /**
     * tessdata 的父目录
     */
    private static String path = "/res/";

    public static void init(){
        mTess = new TessBaseAPI();
        if (mTess.Init(System.getenv("TESSDATA_PREFIX"), "eng") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }
//        String property = System.getProperty("user.dir");
//        String datapath = property + path;
//        if (mTess.Init(datapath, "idcard") != 0){
//            System.err.println("self definition error: Could not initialize tesseract.");
//            System.exit(1);
//        }

//        String whiteList = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz/ ";
//        mTess.SetVariable("tessedit_char_whitelist", whiteList);
//        String whiteListOfNumber = "1234567890";
//        mTess.SetVariable("tessedit_char_whitelist", whiteListOfNumber);
    }

    /**
     * get result string
     * @param f
     * @return
     */
    public static String getOCRText(File f){

        init();

        PIX image = pixRead(f.getAbsolutePath());
        mTess.SetImage(image);

        BytePointer outText = mTess.GetUTF8Text();

        String out = null;
        try {
            out = outText.getString("UTF-8");

            mTess.End();
            outText.deallocate();
            pixDestroy(image);
            mTess.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Could not close tesseract.");
            e.printStackTrace();
        }

        mTess = null;

        return out;
    }
}
