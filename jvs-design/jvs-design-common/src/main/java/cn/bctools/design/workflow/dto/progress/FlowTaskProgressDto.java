package cn.bctools.design.workflow.dto.progress;

import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.model.properties.ApprovalModeProperties;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("任务进度响应-工作流任务进度")
public class FlowTaskProgressDto extends CourseDto {

    @ApiModelProperty(value = "审批模式：AND-会签，NEXT-依次，OR-或签")
    private NodePropertiesModeEnum mode;

    @ApiModelProperty(value = "审批模式配置")
    private ApprovalModeProperties modeProps;

    @ApiModelProperty(value = "节点表单id")
    private String formId;

    @ApiModelProperty(value = "节点表单版本")
    private String formVersion;
}
