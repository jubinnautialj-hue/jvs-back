package cn.bctools.rule.data.postgresql;

import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author wl
 */
@Data
@Inspect
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PgsqlBaseDto {

    @NotNull(message = "数据源不能为空")
    @ParameterValue(info = "目标数据源", type = InputType.selected,  cls = PgsqlSelected.class)
    public String databaseName;

    @NotNull(message = "执行SQL语句不能为空")
    @ParameterValue(info = "SQL执行语句", type = InputType.sql,explain = "内容中如果存在变量,则自动替换,变量示例:${变量名}")
    public String execSql;

    @ParameterValue(info = "参数变量", type = InputType.map,necessity = false)
    public Map<String, Object> param;
}
