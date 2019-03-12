package data.trained;

import debug.Debug;
import imgToText.TessReg;

import java.io.File;

/**
 * Created by chenqiu on 3/12/19.
 */
public class TrainingCheck {

    public static void main(String []args) {
        File f = Debug.newPropertyFile("../dataset/");
        if (f.isDirectory()) {
            File []files = f.listFiles();
            for (File file : files) {
                System.out.println(TessReg.getOCRText(file));
            }
        }
    }
}
