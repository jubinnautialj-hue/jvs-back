package cn.bctools.bi.dto;

import cn.bctools.bi.entity.enums.TaskStatusEnums;
import cn.bctools.bi.entity.enums.TaskTypeEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@ApiModel("导出数据")
@Data
@Accessors(chain = true)
public class DownFileTaskDto {
    @ApiModelProperty("下载类型")
    private TaskTypeEnums type;
    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("任务id")
    private String taskId;
    @ApiModelProperty("数据id")
    private String dataId;
    @ApiModelProperty("任务状态")
    private TaskStatusEnums status;
    @ApiModelProperty("是否下载mock数据")
    private Boolean isMock;
}
