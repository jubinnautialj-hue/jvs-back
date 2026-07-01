package cn.bctools.rule.tools.file;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author zxk
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Base64ConvertFileDto {

    @ParameterValue(info = "base64", type = InputType.longtext)
    public String base64;

    @ParameterValue(info = "文件名称,默认为txt", explain = "请填写包含后缀(.txt)", type = InputType.input)
    public String name;


}
