package cvOverride;

import cvImgUtil.ImgFilter;
import cvImgUtil.ImgSeparator;
import debug.Debug;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.*;

/**
 * Created by chenqiu on 2/20/19.
 */
public class CVRegion extends ImgSeparator {

    public CVRegion(Mat graySrc) {
        super(graySrc);
    }

    final static class Filter extends ImgFilter {
        private Mat src;
        public Filter(Mat src) {
            this.src = src;
        }

        private void sortMap(int [][]a) {
            for (int i = 0; i < a[1].length - 1; i++) {
                int k = i;
                for (int j = i + 1; j < a[1].length; j++) {
                    if (a[1][k] > a[1][j]) {
                        k = j;
                    }
                }
                if (k != i) {
                    a[1][k] = a[1][k] + a[1][i];
                    a[1][i] = a[1][k] - a[1][i];
                    a[1][k] = a[1][k] - a[1][i];
                    a[0][i] = a[0][k] + a[0][i];
                    a[0][k] = a[0][i] - a[0][k];
                    a[0][i] = a[0][i] - a[0][k];
                }
            }
        }

        /**
         * get rect area of id numbers
         * @param contours
         * @return null if rect of id area not found
         */
        public Rect boundingIdRect(List<MatOfPoint> contours) {
            Rect rect;
            List<Rect> rectSet = new ArrayList<>();
            for (int i = 0; i < contours.size(); i++) {
                rect = Imgproc.boundingRect(contours.get(i));
                rectSet.add(rect);
            }
            rect = rectSet.get(0);
            int dist[][] = new int[2][rectSet.size()];
            for (int i = 0; i < rectSet.size(); i++) {
                dist[0][i] = i;
                dist[1][i] = rectSet.get(i).y - rect.y;
            }
            sortMap(dist);
            final int verBias = 15;
            for (int i = 0; i < dist[1].length - 2; i++) {
                if (dist[1][i + 2] - dist[1][i] < verBias) {
                    int k;
                    /**
                     * Upper left and lower right corners
                     */
                    int sx = src.width();
                    int sy = src.height();
                    int mx = -1;
                    int my = -1;
                    // max width between these id-digit area
                    int mw = 0;
                    int sw = src.width();
                    for (k = 0; k < 3; k ++) {
                        rect = rectSet.get(dist[0][k + i]);
                        if (!isDigitRegion(rect, src.width(), src.height())) {
                            break;
                        }
                        sx = Math.min(rect.x, sx);
                        sy = Math.min(rect.y, sy);
                        mx = Math.max(rect.x + rect.width, mx);
                        my = Math.max(rect.y + rect.height, my);
                        mw = Math.max(rect.width, mw);
                        sw = Math.min(rect.width, sw);
                    }
                    // less than 3 area, find next
                    if (k < 3) {
                        continue;
                    }

                    if (i < dist[1].length - 3) {
                        if (dist[1][i + 3] - dist[1][i] < verBias &&
                                isDigitRegion(rect = rectSet.get(dist[0][i + 3]), src.width(), src.height())) {
                            sx = Math.min(sx, rect.x);
                            sy = Math.min(sy, rect.y);
                            mx = Math.max(rect.x + rect.width, mx);
                            my = Math.max(rect.y + rect.height, my);
                            // finding out all 4 digit area
                            return new Rect(sx, sy, mx - sx, my - sy);
                        }
                    }
                    // completing 4th digit area
                    int mg;
                    // in order to make the gap largest, avoiding losing digit message
                    int gap = (mx - sx - sw * 3) >> 1;
                    Rect rt;
                    if (sx < (mg = src.width() - mx - gap)) {
                        rt = mg > mw ? new Rect(sx, sy, mx + mw + gap - sx , my - sy) :
                                new Rect(10, sy, src.width() - 20, my - sy);
                    }
                    else {
                        mg = sx - gap;
                        rt = mg > mw ? new Rect(sx - mw - gap, sy, mx - sx + mw, my - sy) :
                                new Rect(10, sy, src.width() - 20, my - sy);
                    }
                    return rt;
                }
            }
            return null;
        }
    }

    /**
     * for debug
     * getting mat with contours
     * @param src
     * @return
     */
    public static Mat getContours(Mat src) {
        List<MatOfPoint> contours = new ArrayList<>();
        /**
         * TODO:
         * We do findContours for white objects on black background.
         * While your binary image is black chars on white background, you should threshold it with flag THRESH_BINARY_INV to get white on black. Then do findContours.
         */
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat out = Mat.zeros(src.size(), src.type());
        Imgproc.drawContours(out, contours, -1, new Scalar(255, 255, 255), 1);
        return out;
    }

    /**
     * for debug
     * return mat with rectangle drawn
     * @param src
     * @param filter if is null, using CVRegion.Filter
     * @return
     */
    public static Mat drawRectangle(Mat src, RectFilter filter) {
        Mat out = Mat.zeros(src.size(), src.type());
        List<MatOfPoint> contours = new ArrayList<>();
        /**
         * TODO:
         * We do findContours for white objects on black background.
         * While your binary image is black chars on white background, you should threshold it with flag THRESH_BINARY_INV to get white on black. Then do findContours.
         */
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        MatOfPoint contour = null;
        Rect rect = null;
        if (filter == null) {
            filter = new Filter(src);
        }
        for (int i = 0; i < contours.size(); i++) {
            contour = contours.get(i);
            if (filter.isDigitRegion(rect = Imgproc.boundingRect(contour), src.width(), src.height())) {

                Imgproc.rectangle(out, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255), 1);
            }
        }
        return out;
    }

    /**
     * loc the digit area
     * @param src mat proc by binary, top-hat, dilate and closed opr
     * @return
     */
    public Rect digitRegion(Mat src) {
        if (src.cols() < 20 || src.rows() < 20) {
            System.err.println("error: image.cols() < 20 || image.rows() < 20 in function 'digitRegion(Mat m)'");
            System.exit(1);
        }
        fillBorder(src);
        Filter filter = new Filter(src);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Rect rect;
        rect = filter.boundingIdRect(contours);
        if (rect != null) {
            Debug.log(rect);
//            Mat mat = Mat.zeros(src.size(), src.type());
//            Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255), 1);
            return this.rectOfDigitRow = rect;
        }
        // if cannot bounding digit area, start separating rect areas which are large
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                return - ((int)(Imgproc.contourArea(o1) - Imgproc.contourArea(o2))); // decrease
            }
        });
        final int detectDepth = Math.min(5, contours.size());
        Debug.s();
        int maxScore = 0;
        for (int t = 0; t < detectDepth; t++){
            Rect br = Imgproc.boundingRect(contours.get(t));
            Debug.log(br);
            List<Rect> separates = this.rectSeparate(src, br);
            for (Rect r : separates) {
                Mat roi = drawRectRegion(src, r);
                Rect maxRect = filter.findMaxRect(roi);
                int score = filter.IDRegionSimilarity(maxRect, src.rows(), src.cols());
                if (score > maxScore) {
                    maxScore = score;
                    rect = maxRect;
                }
                Debug.log(maxRect + ", score: " + score + ", index: " + t);
            }
        }
        cutEdgeOfX(rect);
        try {
            Debug.e();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.rectOfDigitRow = rect;
    }

    public static Mat drawRectRegion(Mat src, Rect roi) {
        byte buff[] = new byte[src.rows() * src.cols()];
        src.get(0, 0, buff);

        Mat m = Mat.zeros(src.size(), src.type());
        byte out[] = new byte[buff.length];
        int row = roi.y + roi.height;
        for (int i = roi.y; i < row; i++) {
            System.arraycopy(buff, i * src.cols() + roi.x, out, i * src.cols() + roi.x, roi.width);
        }
        m.put(0, 0, out);

        return m;
    }

    public static void fillBorder(Mat m) {
        int cols = m.cols();
        int rows = m.rows();
        final int border = 10;
        byte buff[] = new byte[cols * rows];
        m.get(0, 0, buff);
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if ((i > border && j > border) && (i < cols - border && j < rows - border))
                    continue;
                buff[j * cols + i] = 0;
            }
        }
        m.put(0, 0, buff);
    }

    /**
     * remove left and right edge of id region
     * @param rect
     */
    private void cutEdgeOfX(Rect rect) {
        Mat dst = new Mat();
        Imgproc.GaussianBlur(grayMat, dst, new Size(13, 13), 0);
        Imgproc.Canny(dst, dst, 300, 600, 5, true);
        Imgproc.dilate(dst, dst, new Mat(), new Point(-1, -1), 1);

        Mat m = new Mat(dst, rect);
        byte buff[] = new byte[m.rows() * m.cols()];
        m.get(0, 0, buff);
        int rows = rect.height;
        int cols = rect.width;
        int left = rect.x;
        int right = rect.x + rect.width;
        int w = 0;
        for (int i = 0; i < (cols >> 1); i++) {
            int h = 0;
            for (int j = 0; j < rows; j++) {
                int at = j * cols + i;
                if (buff[at] == 0 && w == 0) {
                    break;
                }
                if (buff[at] != 0) ++h;
            }
            if (w > 0 && h == 0) break;
            if (h == rows) ++w;
            if (w > 0)
                left = rect.x + i;
        }

        byte b[] = new byte[dst.cols() * dst.rows()];
        dst.get(0, 0 ,b);
        if (w > 0) {
            int max = 0;
            for (int i = 0; i < w; i++) {
                int h = extendHeight(b, dst.cols(), left - i, rect.y);
                max = Math.max(max, h);
            }
            // reset
            if (max < rect.height * 1.5)
                left = rect.x;
        }
        // right edge
        w = 0;
        for (int i = cols - 1; i > (cols >> 1); i--) {
            int h = 0;
            for (int j = 0; j < rows; j++) {
                int at = j * cols + i;
                if (buff[at] == 0 && w == 0)
                    break;
                if (buff[at] != 0) ++h;
            }
            if (w > 0 && h == 0) break;
            if (h == rows) w++;
            if (w > 0)
                right = rect.x + i;
        }
        if (w > 0) {
            int max = 0;
            for (int i = 0; i < w; i++) {
                int h = extendHeight(b, dst.cols(), right + i, rect.y);
                max = Math.max(max, h);
            }
            if (max < rect.height * 1.5)
                right = rect.x + rect.width;
        }
        rect.x = left;
        rect.width = right - left;
    }
}
