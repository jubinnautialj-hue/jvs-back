package cn.bctools.design.notice.handler.bo;

import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.workflow.entity.FlowTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("发送通知请求参数")
public class SendNoticeReqBo {

    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("应用id")
    private String appId;
    @ApiModelProperty("模型id")
    private String modelId;
    @ApiModelProperty("数据id")
    private String dataId;
    @ApiModelProperty("消息触发类型")
    private TriggerTypeEnum triggerType;
    @ApiModelProperty("数据")
    private Map<String, Object> data;
    @ApiModelProperty("数据变更key")
    private Collection<String> changeKey;
    @ApiModelProperty("流程任务")
    private FlowTask flowTask;
    @ApiModelProperty("待办任务节点id")
    private List<String> taskNodeIds;
}
