package cn.bctools.rule.tools.ftp;

import cn.bctools.rule.annotations.SelectOption;
import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@SelectOption(RuleConstant.FTP_OPTION)
public class FtpSelectedOption {

    @SelectOptionField("名称")
    public String name;

    @SelectOptionField("用户名")
    public String userName;

    @SelectOptionField(value = "密码", type = InputType.password)
    public String passWord;

    @SelectOptionField("IP")
    public String ip;

    @SelectOptionField("端口")
    public Integer port;
}
