package cn.bctools.rule.tools.paqu;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 网页内容爬取
 *
 * @author guojing
 */
@Accessors(chain = true)
@Data
public class ReptileFunctionDto {

    @ParameterValue(info = "URL地址", necessity = false, type = InputType.text)
    public String url;

    @ParameterValue(info = "html内容", necessity = false, type = InputType.longtext)
    public String body;

    @ParameterValue(info = "标签", necessity = false, type = InputType.input, explain = "直接复制f12的selector 选择器.例:#content_views > p:nth-child(4) > img[src]")
    public String tag;

}
