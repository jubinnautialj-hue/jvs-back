package cn.bctools.design.workflow.dto.manage;

import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zxk
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流任务管理列表入参")
public class TaskManagePageDto {

    @ApiModelProperty(value = "流程名称")
    private String flowName;

    @ApiModelProperty(value = "流程编号")
    private String taskCode;

    @ApiModelProperty(value = "流程标题")
    private String title;

    @ApiModelProperty(value = "模式")
    private AppVersionTypeEnum mode;
}
