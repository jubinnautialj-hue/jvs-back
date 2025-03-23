package cn.bctools.data.factory.util;

import cn.bctools.common.utils.IdGenerator;
import com.alibaba.ttl.TransmittableThreadLocal;

public class ExcelThreadTool {

    private static final ThreadLocal<String> THREAD_TOOL = new TransmittableThreadLocal<>();

    public static String generate(){
        String idStr = IdGenerator.getIdStr();
        THREAD_TOOL.set(idStr);
        return idStr;
    }

    public static String get(){
        return THREAD_TOOL.get();
    }
    public static void remove(){
        THREAD_TOOL.remove();
    }

}
