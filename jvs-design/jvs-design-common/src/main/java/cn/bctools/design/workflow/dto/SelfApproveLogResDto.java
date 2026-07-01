package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.FlowTask;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("我审批的任务记录响应")
public class SelfApproveLogResDto extends FlowTask {

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastApproveTime;

    @ApiModelProperty(value = "发起人头像")
    private String headImg;

    @ApiModelProperty(value = "当前环节节点名")
    private String nodeName;

}
