package cn.bctools.design.rule.impl.fileanalysis;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.tools.file.FileType;
import cn.bctools.rule.tools.file.FileTypeSelected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class FileAnalysisDto {

    @ParameterValue(info = "文件链接地址", explain = "请填写一个完整的文件链接地址",type = InputType.input)
    public String fileUrl;
    @ParameterValue(info = "文件类型", type = InputType.selected, cls = FileTypeSelected.class)
    public FileType fileType;
    @ParameterValue(info = "数据开始行", type = InputType.number, necessity = false, defaultValue = "2")
    public Integer i;
    @ParameterValue(info = "解析表格的别名,key为中文，value为英文", type = InputType.map, necessity = false, explain = "excel解析时使用")
    public Map<String, String> headerAlias;
    @ParameterValue(info = "数据集名称", type = InputType.input, necessity = false, explain = "以模型名称为解析映射关系，并支持组件信息转换,模型名称必需唯一")
    public String modelName;

}
