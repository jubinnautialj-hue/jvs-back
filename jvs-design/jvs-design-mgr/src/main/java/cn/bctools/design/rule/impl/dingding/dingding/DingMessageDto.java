package cn.bctools.design.rule.impl.dingding.dingding;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class DingMessageDto {
    @ParameterValue(info = "用户", explain = "请选择系统用户,用户未绑定钉钉时无法接受到消息",type = InputType.userList)
    @NotEmpty(message = "用户不能为空")
    public List<String> userIds;

    @ParameterValue(info = "标题", necessity = false, explain = "图表可以为空", type = InputType.text)
    public String title;

    @ParameterValue(info = "内容", type = InputType.longtext)
    public String content;

    @ParameterValue(info = "图片地址", necessity = false, explain = "图片可以为空", type = InputType.text)
    public String imgUrl;

    @ParameterValue(info = "用户点击后打开的连接地址", necessity = false, explain = "与图片一同使用", type = InputType.text)
    public String messageUrl;
}
