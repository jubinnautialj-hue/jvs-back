package cn.bctools.design.rule.dto;

import cn.bctools.rule.utils.html.HtmlData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@ApiModel("方法测试的结果")
public class FunctionTestDto {

    @ApiModelProperty("应用ID")
    String jvsAppId;

    @ApiModelProperty("需要执行测试的逻辑节点")
    HtmlData node;

}
