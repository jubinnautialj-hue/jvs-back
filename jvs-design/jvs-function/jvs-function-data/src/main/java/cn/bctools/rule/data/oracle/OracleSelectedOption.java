package cn.bctools.rule.data.oracle;

import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class OracleSelectedOption {

    @SelectOptionField("ip地址")
    public String sourceHost;

    @SelectOptionField("端口")
    public Integer sourcePort;

    @SelectOptionField("SID")
    public String sid;

    @SelectOptionField("Schema")
    public String schema;

    @SelectOptionField("服务名")
    public String serverName;

    @SelectOptionField("用户")
    public String sourceUserName;

    @SelectOptionField(value = "登录密码", type = InputType.password)
    public String sourcePwd;

}
