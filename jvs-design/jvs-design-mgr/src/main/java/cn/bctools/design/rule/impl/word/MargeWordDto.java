package cn.bctools.design.rule.impl.word;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author jvs
 */
@Accessors(chain = true)
@Data
public class MargeWordDto {

    @ParameterValue(info = "模板文件链接地址", explain = "请填写多个文件链接地址信息。", type = InputType.list)
    public List<String> fileUrls;

    @ParameterValue(info = "文件名", type = InputType.input)
    @NotNull(message = "文件名不能为空")
    public String fileName;
    @ParameterValue(info = "是否合并到新页", explain = "默认不合并到新页", type = InputType.onOff, defaultValue = "false")
    public Boolean newPage;
}
