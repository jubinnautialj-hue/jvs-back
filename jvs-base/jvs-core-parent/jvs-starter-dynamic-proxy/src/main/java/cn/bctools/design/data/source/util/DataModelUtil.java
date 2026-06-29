package cn.bctools.design.data.source.util;

import com.baomidou.mybatisplus.core.toolkit.StringPool;

/**
 * @Author: ZhuXiaoKang
 * @Description: 数据模型工具
 */
public class DataModelUtil {

    private DataModelUtil() {
    }

    public static final String SPLIT = StringPool.UNDERSCORE;
    public static final String LOG = "log";
    public static final String DEL = "del";


    /**
     * 构造存储数据日志的集合名称
     *
     * @param modelId 数据模型id
     * @return 集合名
     */
    public static String buildLogCollectionName(String modelId) {
        return LOG + SPLIT + modelId;
    }

    /**
     * 构造存储删除数据的集合名
     *
     * @param modelId 数据模型id
     * @return 集合名
     */
    public static String buildRemoveCollectionName(String modelId) {
        return DEL + SPLIT + modelId;
    }


}
