package cvOCR;

import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.opencv.core.Mat;

/**
 * Created by chenqiu on 3/2/19.
 */
public class Recognizer {

    static tesseract.TessBaseAPI mTess;

    static {
        mTess = new tesseract.TessBaseAPI();
    }

    public Recognizer() {
        if (mTess.Init(System.getenv("TESSDATA_PREFIX") + "/tessdata", "eng") != 0) {
            System.err.println("Recognizer error: Tesseract initialized failed.");
            System.exit(1);
        }
    }

    public String getOCRText(Mat m) {
        return null;
    }
}
