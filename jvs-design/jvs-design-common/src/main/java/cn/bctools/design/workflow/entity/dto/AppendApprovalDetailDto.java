package cn.bctools.design.workflow.entity.dto;

import cn.bctools.auth.api.dto.PersonnelDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 具体加签配置
 */
@Data
@Accessors(chain = true)
@ApiModel("具体加签配置")
public class AppendApprovalDetailDto {

    /**
     * 非加签审批：加签目标为节点id
     * 加签审批，加签目标为当前加签id
     */
    @ApiModelProperty(value = "加签目标id")
    private String appendTargetId;

    @ApiModelProperty(value = "加签id(系统生成)")
    private String id;

    @ApiModelProperty(value = "加签人员")
    private List<PersonnelDto> personnels;

    @ApiModelProperty(value = "加签理由")
    private String description;

    @ApiModelProperty(value = "后置加签id", notes = "为空表示是最后一组加签")
    private String afterId;

    @ApiModelProperty(value = "TURE-已结束, FALSE-未结束")
    private Boolean over = Boolean.FALSE;
}
