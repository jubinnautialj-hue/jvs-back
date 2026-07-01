package cn.bctools.rule.data.mongo;

import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class MongodbDataSourceSelectedOption {

    @NotNull
    @SelectOptionField("链接名称")
    public String name;

    @NotNull
    @SelectOptionField("host")
    public String host;

    @NotNull
    @SelectOptionField("端口")
    public Integer port;

    @NotNull
    @SelectOptionField("用户名")
    public String userName;

    @NotNull
    @SelectOptionField(value = "密码", type = InputType.password)
    public String passWord;

    @NotNull
    @SelectOptionField("数据库名称")
    public String databaseName;

    @NotNull
    @SelectOptionField("集合")
    public String collections;


}
