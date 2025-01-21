package cn.bctools.auth.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class SaveUserDto {

    @ApiModelProperty(value = "用户名称")
    public String realName;

    @ApiModelProperty(value = "用户邮箱")
    public String email;

    @ApiModelProperty(value = "用户性别")
    public String sex;

    @ApiModelProperty(value = "用户帐号")
    public String accountName;

    @ApiModelProperty(value = "用户头像")
    public String headImg;

    @ApiModelProperty(value = "用户id")
    public String userId;

}
