package cn.bctools.design.workflow.utils;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author zhuxiaokang
 */
public class FutureUtil {

    private FutureUtil() {

    }

    /**
     * 等待所有任务执行完毕
     * @param futures
     * @param <T>
     */
    public static <T> void waitAllDone(List<Future<T>> futures) {
        while(true) {
            boolean allDone = Boolean.TRUE;
            for (Future<T> future : futures) {
                if (!future.isDone()) {
                    allDone = Boolean.FALSE;
                }
            }
            if (allDone) {
                break;
            }
        }
    }
}
