package cn.bctools.design.rule.impl.dingding.dingding;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class DingDingDto {

    @NotNull(message = "钉钉Webhook不能为空")
    @ParameterValue(info = "钉钉Webhook", explain = "如果你需要在普通群（即外部群）发送消息时，创建自定义机器人。<a href='https://open.dingtalk.com/document/robots/custom-robot-access'> 查看文档 </a>", type = InputType.selected, cls =
            DingDingSelected.class)
    public String dingDingUrl;

    @ParameterValue(info = "手机号", explain = "手机登录的钉钉号码,此用户需要在钉钉群中否则无法获取到信息", type = InputType.list)
    public List<String> mobiles;

    @ParameterValue(info = "内容", explain = "只支持普通文本", type = InputType.longtext)
    @NotNull(message = "内容不能为空")
    public String content;


}
