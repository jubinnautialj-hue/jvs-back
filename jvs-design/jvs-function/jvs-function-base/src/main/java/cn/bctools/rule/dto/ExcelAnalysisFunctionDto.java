package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.selected.BooleanSelected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ExcelAnalysisFunctionDto {

    @ParameterValue(info = "Excel文件")
    @NotNull(message = "文件不能为空")
    public String fileName;
    @ParameterValue(info = "是否解析文件头", type = InputType.selected, cls = BooleanSelected.class)
    public Boolean title;

}
