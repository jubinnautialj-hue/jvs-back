package cn.bctools.rule.data.mysql.update;

import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.data.mysql.DatasourceSelected;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author guojing
 */
@Data
@Inspect
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DMUpdateDto {

    @NotNull(message = "数据源不能为空")
    @ParameterValue(info = "目标数据源", type = InputType.selected,  cls = DatasourceSelected.class)
    public String databaseName;

    @NotNull(message = "执行SQL语句不能为空")
    @ParameterValue(info = "SQL执行语句", type = InputType.sql)
    public String execSql;
    @ParameterValue(info = "参数变量", type = InputType.map,necessity = false)
    public Map<String, Object> param;

}
