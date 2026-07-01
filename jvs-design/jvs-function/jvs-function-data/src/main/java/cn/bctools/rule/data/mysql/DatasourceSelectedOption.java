package cn.bctools.rule.data.mysql;

import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class DatasourceSelectedOption {

    @SelectOptionField("数据库名称")
    public String databaseName;

    @SelectOptionField("用户名")
    public String userName;

    @SelectOptionField(value = "密码", type = InputType.password)
    public String passWord;

    @SelectOptionField("IP")
    public String ip;

    @SelectOptionField("端口")
    public Integer port;

    
}
