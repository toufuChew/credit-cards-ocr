package cv.imgutils;

import debug.Debug;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * Created by chenqiu on 4/14/19.
 */
public class ImgEditor {

    static class Line {
        private Point p1;
        private Point p2;

        public Line(double x1, double y1, double x2, double y2) {
            p1 = new Point(x1, y1);
            p2 = new Point(x2, y2);
        }

        public Point getPoint(boolean leftTilted) {
            if (p1.x < p2.x)
                return leftTilted ? p2 : p1;
            return leftTilted ? p1 : p2;
        }

        public double tan() {
            return (p2.y - p1.y) / (p2.x - p1.x + 0.01);
        }

        public boolean lefterThan(Line l) {
            if (p1.x + p2.x > l.p1.x + l.p2.x)
                return false;
            return true;
        }

        public boolean righterThan(Line l) {
            return !lefterThan(l);
        }

        public Point getP1() {
            return p1;
        }

        public Point getP2() {
            return p2;
        }
    }

    public static Mat removeEdge(Mat gray0) {
        Line[] lines = getImgEdgeLine(gray0);
        Line left = lines[0];
        Line right = lines[1];
        Point pl = left.getPoint(true);
        Point pr = right.getPoint(false);
        Mat rt = new Mat();
        gray0.submat(new Rect((int)pl.x, 0, (int)(pr.x - pl.x), gray0.rows())).copyTo(rt);
        return rt;
    }

    public static Line[] getImgEdgeLine(Mat gray0) {
        Mat canny = new Mat();
        Imgproc.GaussianBlur(gray0, canny, new Size(13, 13), 0);
        Imgproc.Canny(canny, canny, 300, 600, 5, true);
        Mat lines = new Mat();
        final int minLineLength = (int)(gray0.rows() * 0.3);
        Imgproc.HoughLinesP(canny, lines, 1, Math.PI / 180, 80, minLineLength, 20);
        Line leftLine = new Line(gray0.cols() >> 3, 0, 0, gray0.rows());
        Line rightLine = new Line(gray0.cols(), 0, gray0.cols() - (gray0.cols() >> 3), gray0.rows());

        boolean nonLeft = true;
        boolean nonRight = true;
        Mat view = new Mat(gray0.size(), gray0.type());
        for (int i = 0; i < lines.rows(); i++) {
            double[] points = lines.get(i, 0);
            Line line = new Line(points[0], points[1], points[2], points[3]);
            double tan = line.tan();
            if (Math.abs(tan) < 2.14)
                continue;
            if (!leftLine.lefterThan(line)) {
                leftLine = line;
                nonLeft = false;
            }
            if (!rightLine.righterThan(line)) {
                rightLine = line;
                nonRight = false;
            }
        }
        if (nonLeft) {
            leftLine = new Line(0, 0, 0, gray0.rows());
        }
        if (nonRight) {
            rightLine = new Line(gray0.cols(), 0, gray0.cols(), gray0.rows());
        }
        Imgproc.line(view, leftLine.getP1(), leftLine.getP2(), new Scalar(255, 0, 0), 1);
        Imgproc.line(view, rightLine.getP1(), rightLine.getP2(), new Scalar(255, 0, 0), 1);
        Debug.imshow("lines", view);
        return new Line[]{leftLine, rightLine};
    }
}
