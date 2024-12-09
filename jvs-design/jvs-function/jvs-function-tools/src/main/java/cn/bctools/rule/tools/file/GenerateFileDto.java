package cn.bctools.rule.tools.file;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GenerateFileDto {

    @ParameterValue(info = "文件名称,默认为txt", type = InputType.input)
    public String name;

    @ParameterValue(info = "文件内容", type = InputType.longtext)
    public String text;


}
