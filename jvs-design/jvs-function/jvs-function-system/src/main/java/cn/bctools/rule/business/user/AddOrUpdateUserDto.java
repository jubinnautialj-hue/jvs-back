package cn.bctools.rule.business.user;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author czy
 */
@Data
@Accessors(chain = true)
public class AddOrUpdateUserDto {

    @ParameterValue(info = "用户名称", necessity = false, type = InputType.input)
    public String realName;

    @ParameterValue(info = "用户邮箱", necessity = false, type = InputType.input)
    public String email;

    @ParameterValue(info = "用户性别", explain = "男、女", necessity = false, type = InputType.input)
    public String sex;

    @ParameterValue(info = "用户帐号", necessity = false, type = InputType.input)
    public String accountName;

    @ParameterValue(info = "用户头像", necessity = false, type = InputType.input, defaultValue = "/jvs-ui-public/img/headImg.png")
    public String headImg;

    @ParameterValue(info = "用户id", necessity = false, type = InputType.input)
    public String userId;

}
