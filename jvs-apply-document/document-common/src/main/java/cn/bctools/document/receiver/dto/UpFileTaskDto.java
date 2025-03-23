package cn.bctools.document.receiver.dto;

import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "上传文件")
public class UpFileTaskDto {
    @ApiModelProperty("上级id")
    private String parentId;
    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("桶名称")
    private String bucketName;
    @ApiModelProperty("文件路径")
    private String fileName;
    @ApiModelProperty("日志id")
    private String logId;
    @ApiModelProperty("用户信息")
    private UserDto userDto;
}
