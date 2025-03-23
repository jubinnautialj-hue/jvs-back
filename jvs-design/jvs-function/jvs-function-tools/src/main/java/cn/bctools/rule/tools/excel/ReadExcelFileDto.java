package cn.bctools.rule.tools.excel;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ReadExcelFileDto {

    @ParameterValue(info = "模板文件链接地址", explain = "需要填写完整的链接地址", type = InputType.input)
    public String fileUrl;

    @ParameterValue(info = "sheet", explain = "sheet名称", type = InputType.input)
    public String sheet;

    @ParameterValue(info = "单元格", type = InputType.map, explain = "示例：取单个值示例：参数名(字段名)[A2]、取表格值示例:参数名(字段名)[B7Rx6C(fieldA,fieldB,fieldC,fieldD,fieldE,fieldF)],,默认排除空行")
    public Map<String, Object> cell;

}
