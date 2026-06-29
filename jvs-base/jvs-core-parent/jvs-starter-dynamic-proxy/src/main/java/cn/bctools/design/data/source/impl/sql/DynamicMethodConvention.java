package cn.bctools.design.data.source.impl.sql;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 动态数据接口方法相关约定
 */
public class DynamicMethodConvention {

    /**
     * 新增、修改方法前缀
     */
    public static final List<String> EDIT_METHOD_PREFIX = Arrays.asList("save", "insert", "update");

    /**
     * 查询方法前缀
     */
    public static final List<String> FIND_METHOD_PREFIX = Arrays.asList("find");

    /**
     * 参数：表名
     */
    public static final String PARAM_TABLE_NAME = "collectionName";

    /**
     * 参数：条件
     */
    public static final String PARAM_QUERY = "dynamicQuery";

    /**
     * 参数：数据
     */
    public static final  String PARAM_DATA = "data";
}
