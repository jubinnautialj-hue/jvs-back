package cn.bctools.design.workflow.dto.rule;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("逻辑引擎创建工作流设计入参")
public class FlowDesignRuleCreateDto {
    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "设计id", notes = "已生成过的工作流设计id")
    private String id;

    @ApiModelProperty(value = "应用id", required = true)
    @NotBlank(message = "应用id不能为空")
    private String jvsAppId;

    @ApiModelProperty(value = "节点设计集合", notes = "需保证顺序", required = true)
    @NotEmpty(message = "未设计工作流节点")
    List<Object> nodeObjs;

    @ApiModelProperty(value = "流程名称")
    private String name = "未命名流程";

    @ApiModelProperty(value = "分类")
    private String designGroup = "未分类";
}
