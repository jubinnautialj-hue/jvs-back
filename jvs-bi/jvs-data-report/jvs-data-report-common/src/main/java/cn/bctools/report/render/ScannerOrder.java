package cn.bctools.report.render;

/**
 * 扫描器执行顺序
 * 依赖注解 @Order
 * @author wl
 */
public interface ScannerOrder {

    /**
     * 交叉设计扫描器
     */
    int CROSS_TAB = 5;

    /**
     * 基础设计扫描器
     */
    int NORMAL = 10;

    /**
     * 公式
     */
    int FUNCTION = 15;
}
