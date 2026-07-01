package cn.bctools.design.rule.impl.word;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author guojing
 */
@Accessors(chain = true)
@Data
public class WordDto {

    @ParameterValue(info = "模板文件链接地址", explain = "需要填写完整的链接地址", type = InputType.input)
    public String fileUrl;

    @ParameterValue(info = "模板变量", explain = "文档中的变量名请以${xxx}进行填写否则无法进行替换.",type = InputType.map, subtype = InputType.formula)
    @NotNull(message = "模板变量不能为空")
    public Map<String, Object> body;

    @ParameterValue(info = "文件名", explain = "不包含后缀", type = InputType.input)
    @NotNull(message = "文件名不能为空")
    public String fileName;

    @ParameterValue(info = "输出文件格式", explain = "可将模板转换后的以对应格式输出", type = InputType.selected, cls = WordFileTypeSelected.class)
    public String fileType;

}
