package cn.bctools.data.factory.service;

/**
 * <p>
 * mqtt 抽象类
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
public interface MqttService {
    /**
     * 校验是否在使用
     *
     * @param dataSourceId 数据源id
     * @return 是否使用
     */
    Boolean checkDataSourceUse(String dataSourceId);
    /**
     * 校验是否在使用
     *
     * @param id 主题id 就是表id
     * @return 是否使用
     */
    Boolean checkSubTopicUse(String id);
}
