package cn.bctools.design.workflow.dto.testflow;

import cn.bctools.design.workflow.dto.FlowReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuxiaokang
 * 测试工作流处理操作信息入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("测试工作流处理操作信息入参")
public class FlowTestReqDto extends FlowReqDto {

    @ApiModelProperty(value = "执行用户id")
    private String userId;
}
