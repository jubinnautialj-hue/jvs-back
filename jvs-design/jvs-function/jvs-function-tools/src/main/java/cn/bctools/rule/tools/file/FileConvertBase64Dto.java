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
public class FileConvertBase64Dto {

    @ParameterValue(info = "文件或链接地址", explain = "文件链接地址，或生成的 excel文件或模板文件", type = InputType.input)
    public Object url;

}
