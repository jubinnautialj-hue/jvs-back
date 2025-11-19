package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("抄送给我列表查询入参")
public class CarbonCopyReqDto {
    @ApiModelProperty(value = "流程名称")
    private String flowName;

    @ApiModelProperty(value = "发起人名称")
    private String sendUser;

    @ApiModelProperty(value = "流程编号")
    private String taskCode;
}
