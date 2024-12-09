package cn.bctools.design.data.util;

import cn.bctools.common.utils.SystemThreadLocal;

import java.util.Optional;

/**
 * @author zhuxiaokang
 * 数据模型工具
 */
public class DataModelUtil {

    private DataModelUtil() {
    }

    public static final String SPLIT = "_";

    /**
     * 当前数据版本号key
     */
    private static final String THREAD_LOCAL_DATA_VERSION = "jvsDataVersion";


    /**
     * TRUE-新增/修改数据
     */
    private static final String THREAD_LOCAL_DATA_SAVE= "jvsDataSave";

    /**
     * 构造存储数据日志的集合名称
     *
     * @param modelId 数据模型id
     * @return 集合名
     */
    public static String buildLogCollectionName(String modelId) {
        return "log" + SPLIT + modelId;
    }   public static String buildFollowCollectionName(String modelId) {
        return "follow" + SPLIT + modelId;
    }

    /**
     * 构造存储删除数据的集合名
     *
     * @param modelId 数据模型id
     * @return 集合名
     */
    public static String buildRemoveCollectionName(String modelId) {
        return "del" + SPLIT + modelId;
    }


    /**
     * 设置数据版本号到上下文
     *
     * @param dataVersion 数据版本号
     */
    public static void setCurrentDataVersion(String dataVersion) {
        SystemThreadLocal.set(THREAD_LOCAL_DATA_VERSION, dataVersion);
    }

    /**
     * 从上下文中获取数据版本号
     *
     * @return 数据版本号
     */
    public static String getCurrentDataVersion() {
        return SystemThreadLocal.get(THREAD_LOCAL_DATA_VERSION);
    }

    /**
     * 设置数据操作标识到上下文
     */
    public static void setCurrentSaveData() {
        SystemThreadLocal.set(THREAD_LOCAL_DATA_SAVE, Boolean.TRUE);
    }

    /**
     * 从上下文中获取数据操作标识
     *
     * @return 数据操作类型
     */
    public static Boolean whetherCurrentSaveData() {
        return Optional.ofNullable(SystemThreadLocal.get(THREAD_LOCAL_DATA_SAVE)).map(b -> (Boolean)b).orElseGet(() -> Boolean.FALSE);
    }


}
