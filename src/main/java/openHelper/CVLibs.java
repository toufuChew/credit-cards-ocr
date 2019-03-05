package openHelper;

import org.opencv.core.Mat;

/**
 * Created by chenqiu on 11/26/18.
 */
public interface CVLibs {

    /**
     * RGB 三通道变换为单通道图
     * @param src
     * @return
     */
    Mat rgbToGray(Mat src);

}
