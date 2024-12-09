package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 审批意见
 */
@Data
@Accessors(chain = true)
@ApiModel("审批意见")
public class ApproveOpinionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "意见内容")
    private String content;
}
