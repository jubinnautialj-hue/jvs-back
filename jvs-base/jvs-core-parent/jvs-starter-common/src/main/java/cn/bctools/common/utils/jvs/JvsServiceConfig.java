package cn.bctools.common.utils.jvs;

import cn.bctools.common.enums.ConfigsTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author jvs The type Jvs service config.
 */
@Data
@Accessors(chain = true)
public class JvsServiceConfig {
    /**
     * 服务名
     */
    ConfigsTypeEnum name;
    /**
     * 端口号
     */
    Integer port;
}
