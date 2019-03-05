package imgUtil;

import java.util.List;

/**
 * Created by chenqiu on 10/30/18.
 */
public interface MathUtils {

    double kde(int x, int[] xi, int n);

    /**
     * <p>正态分布在 x 处的取值</p>
     * ø(x) = 1/√2π * exp(-t^2 / 2)
     * @param x
     * @return
     */
    double gaussianDistribution(double x);

    /**
     * <p>查找曲线极值点</p>
     * @param points 曲线各点
     * @return [0] 极大值
     *         <p>[1] 极小值</p>
     */
    List<Object> lineExtreme(double[] points);

    /**
     * <p>查找数组最大值下标</p>
     * @param values
     * @param start 查找的起始位置
     * @param end 查找的终止位置
     * @return
     */
    int findMaxValueIndex(double[] values, int start, int end);
    /**
     * <p>查找数组最小值下标</p>
     * @param values
     * @param start 查找的起始位置
     * @param end 查找的终止位置
     * @return
     */
    int findMinValueIndex(double[] values, int start, int end);
}
