package cn.bctools.document.receiver.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "下载文件")
public class DownFileTaskDto {
    @ApiModelProperty("数据id")
    private String id;
    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("日志id")
    private String logId;
}
