package cn.bctools.remote.constant;

/**
 * @author : GaoZeXi
 */
public interface Constant {
    /**
     * 項目启动时的初始datax同步json key
     */
    String DATAX_JSON_KEY = "dataSource:datax:json";

    /**
     * ods自动生成时间字段 也用于doris 分桶 与排序
     */
    String DORIS_ODS_KEY = "sys_ods_bucket";

    /**
     * 系统自动创建UNIQUE模式唯一key名称
     */
    String DORIS_UNIQUE_FIELD_KEY = "jvs_unique_key";
}
