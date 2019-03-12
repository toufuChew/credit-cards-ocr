package cv.imgutils;

import org.opencv.core.Mat;

import java.io.File;

/**
 * Created by chenqiu on 11/26/18.
 */
public interface SysAsset {

    String DIR_PROPETY = "user.dir";

    String RELATIVE_DIR = "/res/img/origin/";

    String DEBUG_DIR = "/res/img/debug/";

    /**
     * 加载绝对路径文件
     * @param absolutePath
     * @return
     */
    Mat loadFile(String absolutePath);

    Mat loadOriginFile(String fileName);

    Mat loadDebugFile(String fileName);

    void writeFile(File f,  String absolutePath);

    static String originPath() {
        return System.getProperty(DIR_PROPETY) + RELATIVE_DIR;
    }

    static String debugPath() {
        return System.getProperty(DIR_PROPETY) + DEBUG_DIR;
    }
};
