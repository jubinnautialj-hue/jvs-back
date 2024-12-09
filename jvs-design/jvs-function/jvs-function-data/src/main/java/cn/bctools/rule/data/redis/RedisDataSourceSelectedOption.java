package cn.bctools.rule.data.redis;

import cn.bctools.rule.annotations.SelectOption;
import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author zhuxiaokang
 * Redis请求入参
 */

@Data
@Accessors(chain = true)
@SelectOption(RuleConstant.REDIS_DB_SOURCE_OPTION)
public class RedisDataSourceSelectedOption {


    /**
     * 链接名称字段名 必须是 name see DesignController.saveCustomOption 方法
     */
    @NotNull
    @SelectOptionField("redis链接名称")
    public String name;

    @NotNull
    @SelectOptionField("地址")
    public String host;

    @NotNull
    @SelectOptionField("端口")
    public Integer port;

    @SelectOptionField(value = "密码", type = InputType.password)
    public String password;

    @NotNull
    @SelectOptionField("数据库实例下标")
    public Integer database;


}
