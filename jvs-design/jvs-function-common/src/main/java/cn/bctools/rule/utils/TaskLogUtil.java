package cn.bctools.rule.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.dialect.slf4j.Slf4jLog;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * The type Task log util.
 *
 * @author guojing
 * @describe
 */
public class TaskLogUtil extends Slf4jLog {

    /**
     * The type Log.
     */
    @Data
    @Accessors(chain = true)
    public static class Log {
        /**
         * The Msg.
         */
        /*消息*/
        String msg;
        /**
         * The Time.
         */
        /*开始时间*/
        @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
        @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
        LocalDateTime time;
        /**
         * The Type.
         */
        /*执行类型*/
        Type type;


        /**
         * Instantiates a new Log.
         */
        public Log() {
            //默认创建即当前时间
            this.time = LocalDateTime.now();
        }
    }

    /**
     * The enum Type.
     */
    public static enum Type {
        /**
         * trace
         */
        trace,
        /**
         * error
         */
        error,
        /**
         * debug
         */
        debug,
        /**
         * info
         */
        info,
    }

    /**
     * The Thread local.
     */
    static ThreadLocal<LinkedList<Log>> threadLocal = new TransmittableThreadLocal<LinkedList<Log>>();

    /**
     * Instantiates a new Task log util.
     *
     * @param clazz the clazz
     */
    public TaskLogUtil(Class<?> clazz) {
        super(clazz);
    }

    private static LinkedList<Log> getLinkedList() {
        LinkedList<Log> logs = threadLocal.get();
        if (ObjectUtil.isEmpty(logs)) {
            threadLocal.set(new LinkedList<>());
        }
        return threadLocal.get();
    }


    @Override
    public void trace(String format, Object... arguments) {
        String str = StrUtil.format(format, arguments);
        getLinkedList().add(new Log().setMsg(str).setType(Type.trace));
        super.trace(str, arguments);
    }

    @Override
    public void debug(String format, Object... arguments) {
        String str = StrUtil.format(format, arguments);
        getLinkedList().add(new Log().setMsg(str).setType(Type.debug));
        super.debug(str, arguments);
    }


    @Override
    public void info(String format, Object... arguments) {
        String str = StrUtil.format(format, arguments);
        getLinkedList().add(new Log().setMsg(str).setType(Type.info));
        super.info(str, arguments);
    }

    @Override
    public void error(String format, Object... arguments) {
        String str = StrUtil.format(format, arguments);
        getLinkedList().add(new Log().setMsg(str).setType(Type.error));
        super.error(str, arguments);
    }

    /**
     * 支持行数的操作对象
     *
     * @param line      the line
     * @param format    the format
     * @param arguments the arguments
     */
    public void trace(Integer line, String format, Object... arguments) {
        String str = StrUtil.format(format, arguments);
        getLinkedList().add(new Log().setMsg(str).setType(Type.trace));
        super.trace(str, arguments);
    }

    /**
     * Debug.
     *
     * @param line      the line
     * @param format    the format
     * @param arguments the arguments
     */
    public void debug(Integer line, String format, Object... arguments) {
        String str = StrUtil.format(format, arguments);
        getLinkedList().add(new Log().setMsg(str).setType(Type.debug));
        super.debug(str, arguments);
    }

    /**
     * Info.
     *
     * @param line      the line
     * @param format    the format
     * @param arguments the arguments
     */
    public void info(Integer line, String format, Object... arguments) {
        String str = StrUtil.format(format, arguments);
        getLinkedList().add(new Log().setMsg(str).setType(Type.info));
        super.info(str, arguments);
    }

    /**
     * Error.
     *
     * @param line      the line
     * @param format    the format
     * @param arguments the arguments
     */
    public void error(Integer line, String format, Object... arguments) {
        String str = StrUtil.format(format, arguments);
        getLinkedList().add(new Log().setMsg(str).setType(Type.error));
        super.error(str, arguments);
    }

    /**
     * 获取所有的打印日志
     *
     * @return linked list
     */
    public static LinkedList<Log> get() {
        LinkedList<Log> logs = getLinkedList();
        threadLocal.remove();
        return logs;
    }
}
