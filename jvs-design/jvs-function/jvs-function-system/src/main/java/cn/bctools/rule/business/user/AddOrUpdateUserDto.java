package cn.bctools.rule.business.user;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author czy
 */
@Data
@Accessors(chain = true)
public class AddOrUpdateUserDto {

    @ParameterValue(info = "用户姓名", explain = "用户在此组织的昵称。", necessity = false, type = InputType.input)
    public String realName;

    @ParameterValue(info = "用户帐号", explain = "用户在平台的登录帐号，帐号不能重复。", necessity = false, type = InputType.input)
    public String accountName;

    @ParameterValue(info = "用户性别", necessity = false, type = InputType.selected, cls = SexSelectOption.class)
    public String sex;

    @ParameterValue(info = "手机号", necessity = false, type = InputType.input)
    public String phone;

    @ParameterValue(info = "部门id", necessity = false, explain = "多个部门的 id", type = InputType.list)
    public List<String> deptId;

    @ParameterValue(info = "用户邮箱", necessity = false, type = InputType.input)
    public String email;

    @ParameterValue(info = "职位", necessity = false, type = InputType.job)
    public String jobId;

    @ParameterValue(info = "用户头像", necessity = false, type = InputType.input, defaultValue = "/jvs-ui-public/img/headImg.png")
    public String headImg;

    @ParameterValue(info = "用户id", explain = "传递用户id后将使用用户id更新用户信息", necessity = false, type = InputType.input)
    public String userId;
    @ParameterValue(info = "是否离职", explain = "默认是在职、打开后表示离职", defaultValue = "false", necessity = false, type = InputType.onOff)
    public Boolean cancelFlag = false;

}
