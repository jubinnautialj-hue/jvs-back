package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.selected.BooleanSelected;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author guojing
 */
@Accessors(chain = true)
@Data
public class FileTxtParseDto {
    @ParameterValue(info = "上传的文件属性key")
    @NotNull(message = "文件不能为空")
    public String fileName;
    @ParameterValue(info = "是否解析文件头", type = InputType.selected,   cls = BooleanSelected.class)
    public Boolean title;
    @ParameterValue(info = "分割符",explain = "分割符只能存在一个",type = InputType.input, defaultValue = "|")
    public String regex;
}
