package cn.bctools.design.workflow.dto.progress;

import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.oss.dto.BaseFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang 
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("获取打印任务进度数据响应")
public class ProgressPrintResDto extends ApproveResultDto {
    @ApiModelProperty(value = "节点id")
    private String nodeId;

    @ApiModelProperty(value = "节点名")
    private String nodeName;

    @ApiModelProperty(value = "节点最后处理时间")
    private String time;

    @ApiModelProperty(value = "审批结果")
    private String nodeOperation;

    @ApiModelProperty(value = "审批意见")
    private String opinionContent;

    @ApiModelProperty(value = "手写签名")
    private List<BaseFile> opinionSign;
}
