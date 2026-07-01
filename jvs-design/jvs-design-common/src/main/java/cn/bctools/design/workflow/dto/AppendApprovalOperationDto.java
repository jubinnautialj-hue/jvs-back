package cn.bctools.design.workflow.dto;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.design.workflow.model.enums.AppendApprovalPointEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流处理加签操作入参")
public class AppendApprovalOperationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "加签方式")
    private AppendApprovalPointEnum point;

    @ApiModelProperty(value = "加签人员")
    private List<PersonnelDto> personnels;

    @ApiModelProperty(value = "加签理由")
    private String description;
}
