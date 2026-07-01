package cn.bctools.rule.data.mongo;

import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MongoDBOption {

    @SelectOptionField("主机名/IP地址")
    public String host;

    @SelectOptionField("数据库名称")
    public String libraryName;

    @SelectOptionField("用户名")
    public String userName;

    @SelectOptionField(value = "密码",type = InputType.password)
    public String password;

    @SelectOptionField(value = "端口",type = InputType.number)
    public Integer port;

    @SelectOptionField("认证数据库")
    public String authorization;
}
