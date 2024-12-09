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
@ApiModel("获取加签方式响应")
public class AppendApprovalPointResDto {

    @ApiModelProperty(value = "值")
    private String value;
    @ApiModelProperty(value = "名称")
    private String name;
}
