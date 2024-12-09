package cn.bctools.common.enums;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xh
 */
@Data
@Accessors(chain = true)
public class SysConfigLdap extends SysConfigBase<SysConfigLdap> {

    String accountAttribute;
    @ApiModelProperty("base")
    String base;
    @ApiModelProperty("密码")
    String password;
    @ApiModelProperty("url")
    String urls;
    @ApiModelProperty("用户名")
    String username;

}
