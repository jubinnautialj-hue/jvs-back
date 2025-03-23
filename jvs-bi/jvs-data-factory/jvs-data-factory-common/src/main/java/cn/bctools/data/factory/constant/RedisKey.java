package cn.bctools.data.factory.constant;

import cn.bctools.common.utils.TenantContextHolder;
import cn.hutool.core.util.StrUtil;

/**
 * @author admin
 */
public class RedisKey {
    /**
     * 设计阶段的锁
     */
    private static final String DATA_FACTORY_DESIGN_LOCK = "dataFactory:design_lock_{}_{}";
    /**
     * 输入节点的同步数据锁
     */
    private static final String DATA_FACTORY_DESIGN_SYNC_DATA_ODS = "dataFactory:design_sync_data_ods_lock_{}_{}_{}";
    /**
     * 进入队列之前的锁
     */
    private static final String DATA_FACTORY_QUEUE_LOCK = "dataFactory:queue_lock_{}_{}";
    /**
     * mq 监听时的缓存值
     */
    private static final String DATA_FACTORY_MQTT = "dataFactory:mqtt_{}";
    /**
     * 进入队列之前的锁
     */
    private static final String DATA_FACTORY_DATA_SYNC_PLUGIN = "dataFactory:data_sync_plugin";

    /**
     * 输入节点的key
     */
    private static final String DATA_FACTORY_INPUT_LOCK = "dataFactory:input_{}_{}_{}";

    /**
     * 插件key
     */
    public static String getDataFactoryDataSyncPlugin() {
        return DATA_FACTORY_DATA_SYNC_PLUGIN;
    }

    /**
     * mq 监听key
     */
    public static String getDataFactoryMqtt(String id) {
        return StrUtil.format(DATA_FACTORY_MQTT, id);
    }

    /**
     * 设计阶段 防止用户在上次请求没有完成的情况再次点击  造成数据混乱
     *
     * @param id 设计id
     * @return key
     */
    public static String getDataFactoryLockKey(String id) {
        String tenantId = TenantContextHolder.getTenantId();
        return StrUtil.format(DATA_FACTORY_DESIGN_LOCK, id, tenantId);
    }

    /**
     * 数据节点的key 用于设计阶段判断输入是否变更
     *
     * @param id          设计id
     * @param inputNodeId 输入节点id
     * @return key
     */
    public static String getDataFactoryInputKey(String id, String inputNodeId) {
        String tenantId = TenantContextHolder.getTenantId();
        return StrUtil.format(DATA_FACTORY_INPUT_LOCK, id, inputNodeId, tenantId);
    }

    /**
     * 同步设计数据
     *
     * @param id     设计id
     * @param nodeId 节点id
     * @return key
     */
    public static String getSyncDataLock(String id, String nodeId) {
        String tenantId = TenantContextHolder.getTenantId();
        return StrUtil.format(DATA_FACTORY_DESIGN_SYNC_DATA_ODS, id, nodeId, tenantId);
    }

    /**
     * 设计阶段 防止用户在上次请求没有完成的情况再次点击  造成数据混乱
     *
     * @param id 设计id
     * @return key
     */
    public static String getDataFactoryQueueLockKey(String id) {
        String tenantId = TenantContextHolder.getTenantId();
        return StrUtil.format(DATA_FACTORY_QUEUE_LOCK, id, tenantId);
    }
}
