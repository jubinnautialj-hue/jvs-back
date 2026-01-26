package cn.bctools.common.enums;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author xh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysConfigDing extends SysConfigBase<SysConfigDing> {

    @ApiModelProperty(value = "应用appKey")
    private String appKey;
    private String appId;
    @ApiModelProperty(value = "应用Secret")
    private String appSecret;
    @ApiModelProperty(value = "应用agentId")
    private String agentId;

}
