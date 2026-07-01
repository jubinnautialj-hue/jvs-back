package cn.bctools.design.workflow.entity.dto;

import cn.bctools.design.workflow.model.enums.AppendApprovalPointEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 加签
 */
@Data
@Accessors(chain = true)
@ApiModel("加签")
public class AppendApprovalDto {

    @ApiModelProperty(value = "加签位置")
    private AppendApprovalPointEnum appendApprovalPoint;

    @ApiModelProperty(value = "当前加签id")
    private String currentId;

    @ApiModelProperty(value = "加签集合")
    private List<AppendApprovalDetailDto> details;
}
