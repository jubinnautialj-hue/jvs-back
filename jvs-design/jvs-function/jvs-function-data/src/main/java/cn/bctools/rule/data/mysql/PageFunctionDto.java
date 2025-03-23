package cn.bctools.rule.data.mysql;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.data.selected.MysqlEnvironmentVariableSelected;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PageFunctionDto {

    @NotNull(message = "数据源不能为空")
    @ParameterValue(info = "目标数据源", type = InputType.selected,  cls = MysqlEnvironmentVariableSelected.class)
    public String databaseName;

    @NotNull(message = "执行SQL语句不能为空")
    @ParameterValue(info = "SQL执行语句", type = InputType.sql)
    public String execSql;

    @ParameterValue(info = "页码数", type = InputType.number, defaultValue = "1")
    public long current;
    @ParameterValue(info = "页大小", type = InputType.number, defaultValue = "10")
    public long size;

}
