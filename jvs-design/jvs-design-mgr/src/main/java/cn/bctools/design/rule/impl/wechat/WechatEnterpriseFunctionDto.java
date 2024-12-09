package cn.bctools.design.rule.impl.wechat;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class WechatEnterpriseFunctionDto {
    @ParameterValue(info = "选择接收人", type = InputType.userList)
    @NotEmpty(message = "选择接收人不能为空")
    public List<String> userIds;

    @ParameterValue(info = "内容", type = InputType.longtext)
    @NotNull(message = "内容不能为空")
    public String content;
}
