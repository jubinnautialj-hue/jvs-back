package cn.bctools.data.factory.source.util;

import cn.bctools.common.utils.TenantContextHolder;
import cn.hutool.core.util.StrUtil;

/**
 * @author admin
 */
public class RedisKey {
    private static final String DATA_SOURCE_LOCK = "dataSource:save_lock_{}";

    /**
     * 添加默认数据集 数据源是的锁key
     */
    public static String getDataSourceLockKey() {
        String tenantId = TenantContextHolder.getTenantId();
        return StrUtil.format(DATA_SOURCE_LOCK, tenantId);
    }
}
