package cn.bctools.rule.tools.excel;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ExcelFileDto {

    @ParameterValue(info = "文件名", explain = "不包含后缀名", type = InputType.expressionText)
    @NotNull(message = "文件名不能为空")
    public String name;

    @ParameterValue(info = "标题", type = InputType.map)
    @NotNull(message = "标题不能为空")
    public LinkedHashMap<String, String> titleName;

    @ParameterValue(info = "数据", type = InputType.listMap)
    @NotNull(message = "数据不能为空")
    public List<Map<String, Object>> body;

    @ParameterValue(info = "sheet名称", explain = "设置为字段时, 将默认分为多个Sheet", type = InputType.expressionText)
    public String sheet;

}
