package cn.bctools.rule.tools.message;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class MessageDto {
    @ParameterValue(info = "选择接收人", type = InputType.userList)
    @NotEmpty(message = "选择接收人不能为空")
    public List<String> userIds;
    @ParameterValue(info = "标题", type = InputType.input)
    @NotNull(message = "标题不能为空")
    public String title;
    @ParameterValue(info = "内容", type = InputType.longtext)
    @NotNull(message = "内容不能为空")
    public String content;

}
