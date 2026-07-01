package cn.bctools.rule.tools.file;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.OutputType;
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
public class FileDto {

    @ParameterValue(info = "文件链接地址", type = InputType.input)
    public String fileUrl;
    @ParameterValue(info = "文件名称,包含后缀", type = InputType.input)
    public String fileName;
    @ParameterValue(info = "显示类型", type = InputType.selected, cls = FileOutputTypeSelected.class)
    public OutputType fileType;

}
