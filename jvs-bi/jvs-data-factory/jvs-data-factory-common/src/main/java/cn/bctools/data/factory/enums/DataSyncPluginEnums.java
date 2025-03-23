package cn.bctools.data.factory.enums;

import cn.bctools.data.factory.source.data.sync.plugin.DataSyncPlugin;
import cn.bctools.data.factory.source.data.sync.plugin.datax.DataxService;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.SeaTunnelService;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据同步 插件枚举
 *
 * @author Administrator
 */
@Getter
@AllArgsConstructor
public enum DataSyncPluginEnums {
    DATAX_PLUGIN("datax_plugin", "datax数据同步插件", DataxService.class),
    SEA_TUNNEL_PLUGIN("sea_tunnel_plugin", "seaTunnel数据同步插件", SeaTunnelService.class);;
    @EnumValue
    String value;
    String desc;
    Class<? extends DataSyncPlugin> aClass;
}
