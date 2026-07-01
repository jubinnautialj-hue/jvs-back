package cn.bctools.design.workflow.utils;

/**
 * @author zhuxiaokang
 * 工作流线程工具类
 */
public class FlowThreadLocalUtil {

    private FlowThreadLocalUtil() {
    }

    public static void clearAll() {
        FlowContextUtil.context().destroy();
        FlowUtil.clearNodeCache();
        FlowTaskNodeUtil.clear();
    }
}
