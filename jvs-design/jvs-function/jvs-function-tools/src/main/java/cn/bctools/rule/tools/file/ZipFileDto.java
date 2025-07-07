package cn.bctools.rule.tools.file;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ZipFileDto {

    @ParameterValue(info = "文件名称,默认为zip", type = InputType.input)
    public String name;

    @ParameterValue(info = "文件内容", explain = "文件链接数组，或文件组件对象数组", type = InputType.list)
    public List files;


}
