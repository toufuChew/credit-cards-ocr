package cvOverride;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;

/**
 * Created by chenqiu on 2/21/19.
 */
public interface DigitSeparator {

    void digitSeparate(CVFontType.FontType clusterType) throws Exception;
}
