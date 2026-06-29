package cn.bctools.design.data.source.impl.sql;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.source.service.DynamicDDLService;
import cn.bctools.design.data.source.impl.sql.dto.TableColumnCacheDto;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The type Table cache util.
 *
 * @Author: ZhuXiaoKang
 * @Description: 表缓存工具
 */
public class TableCacheUtil {

    private TableCacheUtil() {
    }

    private static final RedisUtils REDIS_UTILS = SpringContextUtil.getBean(RedisUtils.class);
    private static final DynamicDDLService dynamicDDLService = SpringContextUtil.getBean(DynamicDDLService.class);


    /**
     * 表字段缓存
     */
    private static final String TABLE_COLUMN_CACHE_KEY = "dynamic:table:column";

    /***
     * 获取表字段并缓存
     *
     * @param db the db
     * @param collectionName 表名
     * @return 缓存表字段集合 list
     */
    public static List<TableColumnCacheDto> cacheTableColumn(String db, String collectionName) {
        String key = getTableColumnCacheKey(db, collectionName);
        List<TableColumnCacheDto> tableColumns = (List<TableColumnCacheDto>) REDIS_UTILS.get(key);
        if (ObjectNull.isNotNull(tableColumns)) {
            return tableColumns;
        }
        return updateTableColumnCache(db, collectionName);
    }

    /**
     * 修改表字段缓存
     *
     * @param db             the db
     * @param collectionName 表名
     * @return 表字段信息 list
     */
    public static List<TableColumnCacheDto> updateTableColumnCache(String db, String collectionName) {
        String key = getTableColumnCacheKey(db, collectionName);
        List<TableColumnCacheDto> tableColumns = dynamicDDLService.getTableColumn(collectionName);
        REDIS_UTILS.setExpire(key, tableColumns, 30L, TimeUnit.DAYS);
        return tableColumns;
    }

    /**
     * 清空表字段缓存
     *
     * @param db             the db
     * @param collectionName 表名
     */
    public static void clearCacheTableColumn(String db, String collectionName) {
        String key = getTableColumnCacheKey(db, collectionName);
        REDIS_UTILS.del(key);
    }


    /**
     * 获取表字段缓存key
     *
     * @param db
     * @param collectionName
     * @return
     */
    private static String getTableColumnCacheKey(String db, String collectionName) {
        return SysConstant.redisKey(TABLE_COLUMN_CACHE_KEY, db + StringPool.COLON + collectionName);
    }
}
