package cn.bctools.design.rule.impl.wechat;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class WechatMpFunctionDto {
    @ParameterValue(info = "选择模板", explain = "消息模板与系统管理中的消息模板一致。或需要同步微信平台最新的模板信息。", type = InputType.selected, cls = WechatMpSelected.class, linkFields = "body", linkCls = WechatMpLinkSelected.class)
    @NotNull(message = "模板CODE不能为空")
    public String messageTemplateCode;

    @ParameterValue(info = "接收用户", explain = "用户需要提前绑定微信，或同步微信信息", type = InputType.userList)
    @NotEmpty(message = "接收人id不能为空")
    public List<String> userIds;

    @ParameterValue(info = "内容", explain = "微信模板的变量键值对信息", type = InputType.dataModelField)
    @NotNull(message = "内容不能为空")
    public Map<String, String> body;

}
