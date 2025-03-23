package cn.bctools.data.factory.constant;

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
     * seaTunnel公共参数
     */
    String SEA_TUNNEL_JSON_KEY = "dataFactory:seaTunnel:json";

    /**
     * 批次号
     */
    String LOT_NO = "jvs_bi_lot_no";

    /**
     * 系统自动创建UNIQUE模式唯一key名称
     */
    String DORIS_UNIQUE_FIELD_KEY = "jvs_unique_key";
    /**
     * 排序节点key
     */
    String SORT_FIELD = "sortFieldKey";
    /**
     * 正式执行时的线程对象key 用于设置当前执行的数据集对象 方便输入节点进行 血缘视图的传递
     */
    String SYSTEM_NOW_JVS_DATA_FACTORY = "systemNowJvsDataFactory";

    /**
     * 队列名称 血缘视图
     */
    String CONSANGUINITY_ANALYSE_TASK = "consanguinity_analyse_task";

}
