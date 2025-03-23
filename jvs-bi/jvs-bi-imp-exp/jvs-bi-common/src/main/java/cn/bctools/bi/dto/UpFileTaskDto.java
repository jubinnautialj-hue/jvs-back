package cn.bctools.bi.dto;

import cn.bctools.bi.entity.enums.TaskTypeEnums;
import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@ApiModel("导入数据")
@Data
@Accessors(chain = true)
public class UpFileTaskDto {
    @ApiModelProperty("下载类型")
    private TaskTypeEnums type;
    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("任务id")
    private String taskId;
    @ApiModelProperty("目录id")
    private String menuId;
    @ApiModelProperty("桶名称")
    private String bucketName;
    @ApiModelProperty("文件路径")
    private String fileName;
    @ApiModelProperty("用户信息")
    private UserDto userDto;

}
