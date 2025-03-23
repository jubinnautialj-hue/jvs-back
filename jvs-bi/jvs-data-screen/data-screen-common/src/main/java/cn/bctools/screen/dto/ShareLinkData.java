package cn.bctools.screen.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("公开链接")
public class ShareLinkData {
    @ApiModelProperty("长链接")
    private String longLink;
    @ApiModelProperty("短链接")
    private String shortLink;
    @ApiModelProperty("到期时间")
    private String expirationTime;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("数据id")
    private String id;
}
