package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service;

import cn.bctools.data.factory.source.data.sync.plugin.DataSyncPlugin;
import cn.bctools.data.factory.source.dto.RealTimeSettingDto;

/**
 * seaTunnel 数据同步插件
 *
 * @author Administrator
 */
public interface SeaTunnelService extends DataSyncPlugin<RealTimeSettingDto> {

    /**
     * 同步
     * 上游必须把不需要同步的数据过滤掉
     *
     * @param structureId  表结构id
     * @param odsTableName doris 表名称
     * @return seatunnel 任务id
     */
    String syncDataCDC(String structureId, String odsTableName);

}
