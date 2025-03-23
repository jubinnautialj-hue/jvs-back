package cn.bctools.data.factory.source.data.sync.plugin;

import cn.bctools.data.factory.dto.DataSourceField;

import java.util.List;

/**
 * 数据同步插件 公共类
 */

public interface DataSyncPlugin<T> {
    /**
     * 同步
     * 上游必须把不需要同步的数据过滤掉
     *
     * @param structureId        表结构id
     * @param dataSourceFields   需要同步的结构
     * @param size               是否存在数量限制
     * @param dorisTableName     表名称
     * @param incrementalSetting 增量配置
     */
    void syncData(String structureId, List<DataSourceField> dataSourceFields, Long size, String dorisTableName, T incrementalSetting);
}
