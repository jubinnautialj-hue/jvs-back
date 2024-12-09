package cn.bctools.rule.tools.network;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 网络监控服务
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class NetworkMonitoringDto {

    @ParameterValue(info = "网络检测IP或地址", type = InputType.list)
    @NotNull(message = "网络检测IP或地址不能为空")
    public List<String> ips;

}
