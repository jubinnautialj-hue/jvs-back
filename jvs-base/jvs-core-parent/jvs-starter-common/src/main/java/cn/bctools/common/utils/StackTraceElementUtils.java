package cn.bctools.common.utils;

import cn.bctools.common.exception.LicenseException;
import lombok.extern.slf4j.Slf4j;

/**
 * 此工具默认只使用到
 *
 * @author guojing
 */
@Slf4j
public class StackTraceElementUtils {

    /**
     * 此工具可直接获取堆栈方法调用信息
     *
     * @param throwable 堆栈对象
     * @return
     */
    public static String logThrowableToString(Throwable throwable, String sp) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        StringBuffer stringBuffer = new StringBuffer(200);
        for (StackTraceElement stackTraceElement : stackTrace) {
            stringBuffer.append(stackTraceElement + "" + sp);
        }
        String message = throwable.getClass().getName() + ":" + throwable.getMessage() + sp + stringBuffer.toString();
        return message;
    }

    /**
     * 此工具可直接获取堆栈方法调用信息
     *
     * @param throwable 堆栈对象
     * @return
     */
    public static String logThrowableToString(Throwable throwable) {
        if ((throwable instanceof LicenseException)) {
            return throwable.getMessage();
        }
        StringBuffer stringBuffer = new StringBuffer(200);
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stringBuffer.append(stackTraceElement + "\n");
        }
        String message = throwable.getClass().getName() + ":" + throwable.getMessage() + "\n\t" + stringBuffer.toString();
        return message;
    }

    /**
     * 调试插入
     */
    public static void printThread() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuffer stringBuffer = new StringBuffer(200);
            for (StackTraceElement stackTraceElement : stackTrace) {
                stringBuffer.append(stackTraceElement + "\n");
            }
            log.info("打印当前日志信息,{}",stringBuffer);
        } catch (Exception e) {

        }
    }
}
