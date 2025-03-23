package cn.bctools.auth.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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

    @ApiModelProperty("手机号")
    public String phone;

    @ApiModelProperty("部门id")
    public List<String> deptId;

    @ApiModelProperty("职位")
    public String jobId;

    @ApiModelProperty(value = "用户性别")
    public String sex;

    @ApiModelProperty(value = "用户帐号")
    public String accountName;

    @ApiModelProperty(value = "用户头像")
    public String headImg;

    @ApiModelProperty(value = "用户id")
    public String userId;
    @ApiModelProperty(value = "0-正常，1-注销  不要逻辑删除，删除后，业务找不到数据")
    public Boolean cancelFlag = false;

}
